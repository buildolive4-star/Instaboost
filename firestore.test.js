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
  await assertFails(
    unauthDb.collection("catalogServices").where("published", "==", true).get()
  );
  await assertFails(
    unauthDb.collection("coinPackages").where("active", "==", true).get()
  );
});

test("UserProfile: owner can create, read, and perform allowed updates; cross-user & ghost fields denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(5000);

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

  // Allowed profile update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice Pro",
      mascotEnabled: false,
      updatedAt: pastDate(1000),
    })
  );

  // Allowed wallet & streak update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      coins: 515,
      points: 2,
      level: 1,
      dailyStreak: 1,
      lastDailyClaimDay: "2026-11-26",
      updatedAt: pastDate(500),
    })
  );

  // Reject ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: pastDate(200),
    })
  );

  // Reject createdAt mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: pastDate(100),
      updatedAt: pastDate(100),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Future",
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("Subcollections: orders, immutable transactions, favorites, notifications, support tickets", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = pastDate(3000);

  // Orders
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
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("orders").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("ord_1").update({
      contentLines: ["#merrychristmas", "#christmasmagic"],
      updatedAt: pastDate(500),
    })
  );

  // Transactions (immutable ledger)
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
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").update({
      coinDelta: 99999,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").delete()
  );

  // Favorites
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("fav_1").set({
      userId: ALICE_UID,
      serviceId: "christmas_30",
      serviceName: "30 Christmas",
      categoryName: "Christmas",
      contentText: "#merrychristmas #christmastree",
      createdAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("fav_1").delete()
  );

  // Notifications
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").doc("n_1").set({
      userId: ALICE_UID,
      title: "Welcome to CreatorBoost",
      body: "You received 15 welcome bonus coins!",
      category: "REWARD",
      read: false,
      createdAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").doc("n_1").update({
      read: true,
    })
  );

  // Support Tickets
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
  // Owner cannot self-resolve ticket status
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("supportTickets").doc("t_1").update({
      status: "RESOLVED",
      updatedAt: pastDate(500),
    })
  );
});

test("Admin catalog & coinPackages: verified admin writes, signed-in users read published/active only, spoofed email rejected", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "buildolive4@gmail.com",
    email_verified: true,
  }).firestore();
  const spoofAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: "buildolive4@gmail.com",
    email_verified: false,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = pastDate(2000);

  // Spoofed email (email_verified: false) MUST fail
  await assertFails(
    spoofAdminDb.collection("catalogServices").doc("svc_spoof").set({
      categoryId: "food",
      categoryName: "Food",
      name: "Spoof Pack",
      packType: "HASHTAGS",
      itemCount: 10,
      coinPrice: 5,
      trending: true,
      published: true,
      contentLines: ["#foodie"],
      createdAt: now,
      updatedAt: now,
    })
  );

  // Verified admin succeeds
  await assertSucceeds(
    adminDb.collection("catalogServices").doc("svc_1").set({
      categoryId: "food",
      categoryName: "Food",
      name: "20 Street Food",
      packType: "HASHTAGS",
      itemCount: 20,
      coinPrice: 5,
      trending: true,
      published: true,
      contentLines: ["#streetfood", "#foodie"],
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered published query succeeds for regular user; unfiltered fails
  await assertSucceeds(
    aliceDb.collection("catalogServices").where("published", "==", true).get()
  );
  await assertFails(aliceDb.collection("catalogServices").get());

  // Regular user cannot create catalog service
  await assertFails(
    aliceDb.collection("catalogServices").doc("svc_2").set({
      categoryId: "food",
      categoryName: "Food",
      name: "Unauthorized Pack",
      packType: "HASHTAGS",
      itemCount: 10,
      coinPrice: 5,
      trending: false,
      published: true,
      contentLines: ["#food"],
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin coin package
  await assertSucceeds(
    adminDb.collection("coinPackages").doc("pkg_1").set({
      name: "Starter 500",
      coins: 500,
      bonusCoins: 0,
      points: 2,
      priceInr: 249,
      sortOrder: 1,
      active: true,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("coinPackages").where("active", "==", true).get()
  );
  await assertFails(aliceDb.collection("coinPackages").get());
});
