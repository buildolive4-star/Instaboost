import groovy.json.JsonSlurper

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

// Parse shared Firebase config from app/google-services.json & app/src/main/res/values/firebase_applet_config.xml
// so a single GOOGLE_SERVICES_JSON secret powers both User Panel (:app) and Admin Panel (:admin).
val adminPackageName = "com.aistudio.creatorboost.mrxvkp.admin"
val gsFile = rootProject.file("app/google-services.json")
val fbAppletXml = rootProject.file("app/src/main/res/values/firebase_applet_config.xml")

var fbProjectId = ""
var fbProjectNumber = ""
var fbStorageBucket = ""
var fbAppId = ""
var fbApiKey = ""
var fbWebClientId = ""
var fbDatabaseId = "(default)"

if (gsFile.exists()) {
  @Suppress("UNCHECKED_CAST")
  val root = JsonSlurper().parse(gsFile) as Map<String, Any?>
  @Suppress("UNCHECKED_CAST")
  val projectInfo = root["project_info"] as? Map<String, Any?>
  fbProjectId = projectInfo?.get("project_id")?.toString().orEmpty()
  fbProjectNumber = projectInfo?.get("project_number")?.toString().orEmpty()
  fbStorageBucket = projectInfo?.get("storage_bucket")?.toString().orEmpty()

  @Suppress("UNCHECKED_CAST")
  val clients = root["client"] as? List<Map<String, Any?>> ?: emptyList()
  val preferredClient = clients.firstOrNull { client ->
    @Suppress("UNCHECKED_CAST")
    val info = client["client_info"] as? Map<String, Any?>
    @Suppress("UNCHECKED_CAST")
    val androidInfo = info?.get("android_client_info") as? Map<String, Any?>
    androidInfo?.get("package_name")?.toString() == adminPackageName
  } ?: clients.firstOrNull()

  if (preferredClient != null) {
    @Suppress("UNCHECKED_CAST")
    val clientInfo = preferredClient["client_info"] as? Map<String, Any?>
    fbAppId = clientInfo?.get("mobilesdk_app_id")?.toString().orEmpty()

    @Suppress("UNCHECKED_CAST")
    val apiKeys = preferredClient["api_key"] as? List<Map<String, Any?>> ?: emptyList()
    fbApiKey = apiKeys.firstOrNull()?.get("current_key")?.toString().orEmpty()

    @Suppress("UNCHECKED_CAST")
    val oauthClients = preferredClient["oauth_client"] as? List<Map<String, Any?>> ?: emptyList()
    fbWebClientId = oauthClients.firstOrNull {
      (it["client_type"] as? Number)?.toInt() == 3
    }?.get("client_id")?.toString().orEmpty()

    if (fbWebClientId.isBlank()) {
      @Suppress("UNCHECKED_CAST")
      val services = preferredClient["services"] as? Map<String, Any?>
      @Suppress("UNCHECKED_CAST")
      val appInvite = services?.get("appinvite_service") as? Map<String, Any?>
      @Suppress("UNCHECKED_CAST")
      val otherClients = appInvite?.get("other_platform_oauth_client") as? List<Map<String, Any?>> ?: emptyList()
      fbWebClientId = otherClients.firstOrNull {
        (it["client_type"] as? Number)?.toInt() == 3
      }?.get("client_id")?.toString().orEmpty()
    }
  }
}

if (fbAppletXml.exists()) {
  val xmlText = fbAppletXml.readText()
  val match = Regex("""name="firestore_database_id"[^>]*>([^<]+)</string>""").find(xmlText)
  if (match != null) {
    fbDatabaseId = match.groupValues[1].trim()
  }
}

android {
  namespace = "com.example.admin"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = adminPackageName
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    resValue("string", "firebase_project_id", fbProjectId)
    resValue("string", "firebase_gcm_sender_id", fbProjectNumber)
    resValue("string", "firebase_storage_bucket", fbStorageBucket)
    resValue("string", "firebase_app_id", fbAppId)
    resValue("string", "firebase_api_key", fbApiKey)
    resValue("string", "default_web_client_id", fbWebClientId)
    resValue("string", "firestore_database_id", fbDatabaseId)
  }

  signingConfigs {
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
    resValues = true
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.auth)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
