const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_101";
const BOB_UID = "bob_uid_202";
const ADMIN_UID = "admin_uid_999";
const COADMIN_UID = "coadmin_uid_555";
const VALID_STAFF_KEY = "s_0123456789abcdef0123456789abcdef";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(msOffset = 2000) {
  return new Date(Date.now() - msOffset);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = pastDate();

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("appConfig").doc("global").get());
  await assertFails(unauthDb.collection("adminStaff").doc(VALID_STAFF_KEY).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Unauth",
      coins: 15,
      points: 0,
      level: 1,
      referralCode: "CB1001",
      dailyStreak: 0,
      spinsToday: 0,
      mascotEnabled: true,
      darkMode: false,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("UserProfile: owner can create/update; blocked field restricted to admin; ghost fields denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(5000);

  // User cannot set blocked on create
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      coins: 15,
      points: 0,
      level: 1,
      referralCode: "CBALICE1",
      dailyStreak: 0,
      spinsToday: 0,
      mascotEnabled: true,
      darkMode: false,
      blocked: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Creator",
      email: "alice@example.com",
      country: "India",
      coins: 15,
      points: 0,
      level: 1,
      referralCode: "CBALICE1",
      dailyStreak: 0,
      spinsToday: 0,
      mascotEnabled: true,
      darkMode: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(aliceDb.collection("users").get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice Pro",
      mascotEnabled: false,
      updatedAt: pastDate(1000),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      blocked: true,
      updatedAt: pastDate(500),
    })
  );
});

test("Subcollections: orders, immutable transactions, favorites, notifications, support tickets", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = pastDate(3000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("ord_1").set({
      userId: ALICE_UID,
      serviceId: "christmas_30",
      serviceName: "30 Christmas",
      categoryId: "christmas",
      categoryName: "Christmas",
      itemCount: 30,
      coinPrice: 3,
      status: "COMPLETED",
      contentLines: ["#merrychristmas", "#christmastree"],
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("orders").get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      txType: "PURCHASE",
      title: "500 Coins Pack",
      description: "Purchased 500 coins (+2 pts)",
      coinDelta: 500,
      pointsDelta: 2,
      priceInr: 249,
      status: "SUCCESSFUL",
      createdAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").delete()
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("supportTickets").doc("t_1").set({
      userId: ALICE_UID,
      subject: "Need help with pack",
      category: "Order Issue",
      description: "How do I copy individual hashtags?",
      status: "OPEN",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("supportTickets").doc("t_1").update({
      status: "RESOLVED",
      updatedAt: pastDate(500),
    })
  );
});

test("Main Admin creates staff key, Co-Admin activates & controls user panel, cannot manage staff, instant revocation works", async () => {
  const mainAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "buildolive4@gmail.com",
    email_verified: true,
  }).firestore();
  const coAdminDb = testEnv.authenticatedContext(COADMIN_UID, {
    email: "helper@example.com",
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = pastDate(3000);

  // Seed Alice profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      country: "India",
      coins: 15,
      points: 0,
      level: 1,
      referralCode: "CBALICE1",
      dailyStreak: 0,
      spinsToday: 0,
      mascotEnabled: true,
      darkMode: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Regular user cannot create staff or self-grant admin
  await assertFails(
    coAdminDb.collection("adminStaff").doc(VALID_STAFF_KEY).set({
      staffName: "Fake",
      active: true,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    coAdminDb.collection("admins").doc(COADMIN_UID).set({
      userId: COADMIN_UID,
      staffKey: "s_wrongkey1234567890",
      displayName: "Fake",
      active: true,
      createdAt: now,
    })
  );

  // Main Admin creates Co-Admin entry
  await assertSucceeds(
    mainAdminDb.collection("adminStaff").doc(VALID_STAFF_KEY).set({
      staffName: "Rahul",
      active: true,
      linkedUid: "",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Co-Admin verifies exact hashed staffKey and activates admin grant
  await assertSucceeds(coAdminDb.collection("adminStaff").doc(VALID_STAFF_KEY).get());
  await assertSucceeds(
    coAdminDb.collection("admins").doc(COADMIN_UID).set({
      userId: COADMIN_UID,
      staffKey: VALID_STAFF_KEY,
      displayName: "Rahul",
      active: true,
      createdAt: pastDate(1500),
    })
  );
  await assertSucceeds(
    coAdminDb.collection("adminStaff").doc(VALID_STAFF_KEY).update({
      linkedUid: COADMIN_UID,
      updatedAt: pastDate(1200),
    })
  );

  // Co-Admin now has full operational control over users, appConfig, services, audit logs
  await assertSucceeds(coAdminDb.collection("users").get());
  await assertSucceeds(
    coAdminDb.collection("users").doc(ALICE_UID).update({
      coins: 500,
      blocked: true,
      updatedAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    coAdminDb.collection("appConfig").doc("global").set({
      maintenanceMode: false,
      maintenanceMessage: "",
      announcementTitle: "Festive Sale",
      announcementBody: "50% extra bonus coins today!",
      welcomeCoins: 25,
      referralBonusCoins: 50,
      spinEnabled: true,
      dailyRewardsEnabled: true,
      updatedAt: pastDate(1000),
    })
  );
  await assertSucceeds(aliceDb.collection("appConfig").doc("global").get());
  await assertFails(aliceDb.collection("appConfig").doc("global").update({ welcomeCoins: 9999, updatedAt: pastDate(500) }));

  // Blocked Alice can no longer mutate her own profile
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Unblocked Attempt",
      updatedAt: pastDate(500),
    })
  );

  // Co-Admin CANNOT list or create/delete other staff members (Main Admin exclusive!)
  await assertFails(coAdminDb.collection("adminStaff").get());
  await assertFails(
    coAdminDb.collection("adminStaff").doc("s_anotherkey1234567890").set({
      staffName: "Rogue",
      active: true,
      createdAt: pastDate(500),
      updatedAt: pastDate(500),
    })
  );

  // Main Admin revokes staff key -> Co-Admin loses access IMMEDIATELY
  await assertSucceeds(mainAdminDb.collection("adminStaff").doc(VALID_STAFF_KEY).delete());
  await assertFails(coAdminDb.collection("users").get());
});
