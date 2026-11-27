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

  // Co-Admin creates/edits dynamic category & service with providerName and photoUrl
  await assertSucceeds(
    coAdminDb.collection("catalogCategories").doc("reels_boost").set({
      name: "Reels Boost",
      subtitle: "Trending Reels Packs",
      description: "High reach reels growth tags",
      emoji: "🎬",
      photoUrl: "https://example.com/reels.jpg",
      sortOrder: 1,
      active: true,
      createdAt: pastDate(1000),
      updatedAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    aliceDb.collection("catalogCategories").where("active", "==", true).get()
  );
  await assertFails(
    aliceDb.collection("catalogCategories").doc("reels_boost").update({
      name: "Hacked",
      updatedAt: pastDate(500),
    })
  );
  await assertSucceeds(
    coAdminDb.collection("catalogServices").doc("svc_pro_1").set({
      categoryId: "reels_boost",
      categoryName: "Reels Boost",
      name: "50 Viral Reels Tags",
      packType: "HASHTAGS",
      itemCount: 2,
      coinPrice: 20,
      trending: true,
      published: true,
      providerName: "Fame Maker Pro",
      photoUrl: "https://example.com/pack.jpg",
      contentLines: ["#reels", "#viral"],
      createdAt: pastDate(1000),
      updatedAt: pastDate(1000),
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

test("ServiceFiles, ServicePlans, ServiceOrders, FundRequests, and zero-trust INR walletBalancePaise", async () => {
  const mainAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "buildolive4@gmail.com",
    email_verified: true,
  }).firestore();
  const coAdminDb = testEnv.authenticatedContext(COADMIN_UID, {
    email: "helper@example.com",
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = pastDate(3000);

  // Setup Co-Admin grant
  await assertSucceeds(
    mainAdminDb.collection("adminStaff").doc(VALID_STAFF_KEY).set({
      staffName: "CoAdminOps",
      active: true,
      linkedUid: "",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    coAdminDb.collection("admins").doc(COADMIN_UID).set({
      userId: COADMIN_UID,
      staffKey: VALID_STAFF_KEY,
      displayName: "CoAdminOps",
      active: true,
      createdAt: pastDate(2000),
    })
  );

  // User cannot create account with non-zero walletBalancePaise
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      coins: 15,
      walletBalancePaise: 50000,
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

  // Valid user profile with 0 paise balance
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      country: "India",
      coins: 15,
      walletBalancePaise: 0,
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

  // User cannot self-credit INR wallet balance
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      walletBalancePaise: 10000,
      updatedAt: pastDate(1500),
    })
  );

  // Co-Admin creates ServiceFile, ServicePlan, and UPI AppConfig
  await assertSucceeds(
    coAdminDb.collection("serviceFiles").doc("ig_reel_views").set({
      categoryId: "instagram",
      categoryName: "Instagram",
      name: "Instagram Reel Views",
      subtitle: "Superfast real dashboard views",
      emoji: "🎬",
      photoUrl: "https://i.ibb.co/example/reel.jpg",
      sortOrder: 1,
      active: true,
      createdAt: pastDate(1800),
      updatedAt: pastDate(1800),
    })
  );
  await assertSucceeds(
    coAdminDb.collection("servicePlans").doc("plan_reel_superfast").set({
      categoryId: "instagram",
      categoryName: "Instagram",
      fileId: "ig_reel_views",
      fileName: "Instagram Reel Views",
      name: "IG Reel Views - Superfast ⚡",
      description: "Instant start with dashboard insights",
      photoUrl: "https://i.ibb.co/example/plan.jpg",
      pricePer1kPaise: 100,
      minQty: 100,
      maxQty: 100000,
      inputHint: "Enter Instagram Reel link",
      presetTiers: ["100:0", "500:0", "1000:0", "2000:5", "5000:6"],
      sortOrder: 1,
      active: true,
      createdAt: pastDate(1800),
      updatedAt: pastDate(1800),
    })
  );
  await assertSucceeds(
    coAdminDb.collection("appConfig").doc("global").set({
      maintenanceMode: false,
      maintenanceMessage: "",
      announcementTitle: "UPI Instant Deposit",
      announcementBody: "Add funds via UPI QR and start growing!",
      welcomeCoins: 15,
      referralBonusCoins: 25,
      spinEnabled: true,
      dailyRewardsEnabled: true,
      upiId: "famemaker@upi",
      upiMerchantName: "Fame Maker",
      paymentQrUrl: "https://i.ibb.co/example/qr.jpg",
      paymentInstructions: "Scan QR, pay, and submit 12-digit UTR.",
      minDepositInr: 10,
      supportLink: "https://t.me/famemakersupport",
      updatedAt: pastDate(1700),
    })
  );

  // User reads active files & plans, cannot mutate them
  await assertSucceeds(aliceDb.collection("serviceFiles").where("active", "==", true).get());
  await assertSucceeds(aliceDb.collection("servicePlans").where("active", "==", true).get());
  await assertFails(
    aliceDb.collection("servicePlans").doc("plan_reel_superfast").update({
      pricePer1kPaise: 1,
      updatedAt: pastDate(500),
    })
  );

  // Alice submits a FundRequest (PENDING only)
  await assertFails(
    aliceDb.collection("fundRequests").doc("fr_bad").set({
      userId: ALICE_UID,
      userName: "Alice",
      amountPaise: 10000,
      utrNumber: "412345678901",
      status: "APPROVED",
      createdAt: pastDate(1500),
      updatedAt: pastDate(1500),
    })
  );
  await assertSucceeds(
    aliceDb.collection("fundRequests").doc("fr_1").set({
      userId: ALICE_UID,
      userName: "Alice",
      userEmail: "alice@example.com",
      amountPaise: 10000,
      utrNumber: "412345678901",
      proofImageUrl: "https://i.ibb.co/example/proof.jpg",
      status: "PENDING",
      createdAt: pastDate(1500),
      updatedAt: pastDate(1500),
    })
  );
  // Bob cannot read Alice's fund request
  await assertFails(bobDb.collection("fundRequests").doc("fr_1").get());
  // Alice cannot self-approve
  await assertFails(
    aliceDb.collection("fundRequests").doc("fr_1").update({
      status: "APPROVED",
      updatedAt: pastDate(1200),
    })
  );

  // Co-Admin approves fund request and credits Alice's walletBalancePaise
  await assertSucceeds(
    coAdminDb.collection("fundRequests").doc("fr_1").update({
      status: "APPROVED",
      adminNote: "Verified UTR",
      updatedAt: pastDate(1200),
    })
  );
  await assertSucceeds(
    coAdminDb.collection("users").doc(ALICE_UID).update({
      walletBalancePaise: 10000,
      updatedAt: pastDate(1200),
    })
  );

  // Alice spends ₹1.90 (190 paise) from wallet to place a ServiceOrder
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      walletBalancePaise: 9810,
      updatedAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    aliceDb.collection("serviceOrders").doc("so_1").set({
      userId: ALICE_UID,
      userName: "Alice",
      userEmail: "alice@example.com",
      categoryId: "instagram",
      categoryName: "Instagram",
      fileId: "ig_reel_views",
      fileName: "Instagram Reel Views",
      planId: "plan_reel_superfast",
      planName: "IG Reel Views - Superfast ⚡",
      targetLink: "https://instagram.com/reel/xyz123",
      quantity: 2000,
      totalPricePaise: 190,
      paymentMethod: "WALLET",
      status: "PENDING",
      createdAt: pastDate(1000),
      updatedAt: pastDate(1000),
    })
  );
  // Alice can list her own orders, Bob cannot
  await assertSucceeds(
    aliceDb.collection("serviceOrders").where("userId", "==", ALICE_UID).get()
  );
  await assertFails(bobDb.collection("serviceOrders").doc("so_1").get());
  // Alice cannot self-complete her order
  await assertFails(
    aliceDb.collection("serviceOrders").doc("so_1").update({
      status: "COMPLETED",
      updatedAt: pastDate(500),
    })
  );
  // Co-Admin updates order status to IN_PROGRESS and COMPLETED
  await assertSucceeds(
    coAdminDb.collection("serviceOrders").doc("so_1").update({
      status: "COMPLETED",
      adminNote: "Delivered 2000 views",
      updatedAt: pastDate(500),
    })
  );
});

test("Null-doc get() regression, coinRatePerInr config, COIN_PACK UPI approval, and instant INR wallet -> coin purchase", async () => {
  // Also test case-insensitive Main Admin email
  const mainAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "BuildOlive4@gmail.com",
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = pastDate(3000);

  // Regression: Admin calling get() on non-existent catalogCategories / serviceFiles / fundRequests doc must NOT fail
  await assertSucceeds(mainAdminDb.collection("catalogCategories").doc("brand_new_cat").get());
  await assertSucceeds(mainAdminDb.collection("serviceFiles").doc("brand_new_file").get());
  await assertSucceeds(mainAdminDb.collection("servicePlans").doc("brand_new_plan").get());

  // Admin saves appConfig with coinRatePerInr
  await assertSucceeds(
    mainAdminDb.collection("appConfig").doc("global").set({
      maintenanceMode: false,
      maintenanceMessage: "",
      announcementTitle: "Coin Recharge Open",
      announcementBody: "Pay via UPI or INR wallet to get coins!",
      welcomeCoins: 15,
      referralBonusCoins: 25,
      spinEnabled: true,
      dailyRewardsEnabled: true,
      upiId: "famemaker@upi",
      upiMerchantName: "Fame Maker",
      paymentQrUrl: "https://i.ibb.co/example/qr.jpg",
      paymentInstructions: "Scan QR and enter 12-digit UTR.",
      minDepositInr: 10,
      coinRatePerInr: 3,
      supportLink: "https://t.me/famemakersupport",
      updatedAt: pastDate(2000),
    })
  );

  // Seed Alice profile with 0 paise and 15 coins
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      country: "",
      coins: 15,
      walletBalancePaise: 0,
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

  // Alice submits a COIN_PACK UPI payment request (PENDING)
  await assertSucceeds(
    aliceDb.collection("fundRequests").doc("coin_req_1").set({
      userId: ALICE_UID,
      userName: "Alice",
      userEmail: "alice@example.com",
      amountPaise: 49900,
      utrNumber: "512345678901",
      proofImageUrl: "https://i.ibb.co/example/coin-proof.jpg",
      status: "PENDING",
      requestType: "COIN_PACK",
      packageId: "pack_1000",
      packageName: "1,000 Coins Pack",
      coinsToCredit: 1250,
      pointsToCredit: 5,
      createdAt: pastDate(1500),
      updatedAt: pastDate(1500),
    })
  );

  // Admin approves COIN_PACK request and credits Alice's coins, points, and level
  await assertSucceeds(
    mainAdminDb.collection("fundRequests").doc("coin_req_1").update({
      status: "APPROVED",
      adminNote: "UPI verified, 1250 coins credited",
      updatedAt: pastDate(1200),
    })
  );
  await assertSucceeds(
    mainAdminDb.collection("users").doc(ALICE_UID).update({
      coins: 1265,
      points: 5,
      level: 1,
      updatedAt: pastDate(1200),
    })
  );

  // Give Alice ₹100 (10000 paise) in INR wallet via Admin
  await assertSucceeds(
    mainAdminDb.collection("users").doc(ALICE_UID).update({
      walletBalancePaise: 10000,
      updatedAt: pastDate(1100),
    })
  );

  // Alice buys coins instantly using ₹50 (5000 paise) from her INR wallet (Action 4)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      walletBalancePaise: 5000,
      coins: 1415,
      points: 7,
      level: 1,
      updatedAt: pastDate(900),
    })
  );

  // Alice CANNOT increase her INR walletBalancePaise while buying coins
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      walletBalancePaise: 99999,
      coins: 2000,
      points: 10,
      level: 2,
      updatedAt: pastDate(500),
    })
  );
});
