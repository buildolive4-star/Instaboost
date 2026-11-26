package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.R

object DefaultCatalog {

    val levelTiers: List<LevelTier> = listOf(
        LevelTier(1, "Beginner", 0L, "Welcome to CreatorBoost"),
        LevelTier(2, "Starter", 25L, "Unlocks Starter Creator Badge"),
        LevelTier(3, "Explorer", 75L, "+5% Daily Bonus Energy"),
        LevelTier(4, "Creator", 150L, "Pro Bio Studio Preset"),
        LevelTier(5, "Influencer", 300L, "Priority Trending Packs"),
        LevelTier(6, "Pro Creator", 500L, "Exclusive Festival Packs"),
        LevelTier(7, "Elite", 800L, "VIP Creator Crown"),
        LevelTier(8, "Champion", 1200L, "Master Content Vault"),
        LevelTier(9, "Master", 1800L, "Diamond Flame Aura"),
        LevelTier(10, "Legend", 2500L, "Ultimate CreatorBoost Legend")
    )

    fun calculateLevel(points: Long): Long {
        var matched = 1
        for (tier in levelTiers) {
            if (points >= tier.requiredPoints) matched = tier.level
        }
        return matched.toLong()
    }

    fun pointsToNextLevel(points: Long): Pair<Int, Long> {
        val current = calculateLevel(points).toInt()
        val nextTier = levelTiers.firstOrNull { it.level == current + 1 }
        return if (nextTier == null) {
            current to 0L
        } else {
            nextTier.level to (nextTier.requiredPoints - points).coerceAtLeast(0L)
        }
    }

    fun levelProgressFraction(points: Long): Float {
        val current = calculateLevel(points).toInt()
        val currentTier = levelTiers.firstOrNull { it.level == current } ?: return 1f
        val nextTier = levelTiers.firstOrNull { it.level == current + 1 } ?: return 1f
        val span = (nextTier.requiredPoints - currentTier.requiredPoints).coerceAtLeast(1L)
        return ((points - currentTier.requiredPoints).toFloat() / span.toFloat()).coerceIn(0f, 1f)
    }

    val dailyRewards: List<DailyRewardDay> = listOf(
        DailyRewardDay(1, 5L, 1L),
        DailyRewardDay(2, 10L, 1L),
        DailyRewardDay(3, 15L, 2L),
        DailyRewardDay(4, 20L, 2L),
        DailyRewardDay(5, 25L, 3L),
        DailyRewardDay(6, 35L, 4L),
        DailyRewardDay(7, 50L, 10L, "Bonus Badge")
    )

    val spinSegments: List<SpinSegment> = listOf(
        SpinSegment("5 Coins", 5L, 1L, Color(0xFFFFD166), 28),
        SpinSegment("10 Coins", 10L, 2L, Color(0xFFF14A73), 24),
        SpinSegment("Try Again", 0L, 0L, Color(0xFFE5E7EB), 14),
        SpinSegment("20 Coins", 20L, 3L, Color(0xFF477EEB), 14),
        SpinSegment("+5 Pts", 0L, 5L, Color(0xFF34D399), 10),
        SpinSegment("50 Coins", 50L, 5L, Color(0xFFF6BE35), 6),
        SpinSegment("100 Coins", 100L, 10L, Color(0xFFD10F46), 4)
    )

    val categories: List<ServiceCategoryUi> = listOf(
        ServiceCategoryUi(
            id = "food",
            name = "Food",
            subtitle = "Trending hashtags",
            description = "Mouth-watering foodie hashtags, recipe captions, and restaurant reel hooks.",
            imageRes = R.drawable.img_cat_food_1795714396167,
            gradientStart = Color(0xFFFF8C42),
            gradientEnd = Color(0xFF8E2A0B),
            bannerTint = Color(0xFFFFEFE2),
            emoji = "🍔",
            trending = true
        ),
        ServiceCategoryUi(
            id = "christmas",
            name = "Christmas",
            subtitle = "Trending hashtags",
            description = "Festive holiday hashtags, cozy winter captions, and gift reel prompts.",
            imageRes = R.drawable.img_cat_christmas_1795714397033,
            gradientStart = Color(0xFFD10F46),
            gradientEnd = Color(0xFF1F5E3B),
            bannerTint = Color(0xFFDCEBF2),
            emoji = "🎄",
            trending = true
        ),
        ServiceCategoryUi(
            id = "fashion",
            name = "Fashion",
            subtitle = "Trending hashtags",
            description = "Editorial style tags, OOTD captions, and aesthetic fashion creator bios.",
            imageRes = R.drawable.img_cat_fashion_1795714396951,
            gradientStart = Color(0xFF9B6BFF),
            gradientEnd = Color(0xFF512DA8),
            bannerTint = Color(0xFFF0E8FF),
            emoji = "👗",
            trending = true
        ),
        ServiceCategoryUi(
            id = "photography",
            name = "Photography",
            subtitle = "Trending hashtags",
            description = "Portrait, street, DSLR, and golden-hour photography tag packs.",
            imageRes = null,
            gradientStart = Color(0xFF334155),
            gradientEnd = Color(0xFF0F172A),
            bannerTint = Color(0xFFE6ECF5),
            emoji = "📷",
            trending = false
        ),
        ServiceCategoryUi(
            id = "funny",
            name = "Funny",
            subtitle = "Trending hashtags",
            description = "Viral meme hashtags, witty one-liners, and relatable comedy reel ideas.",
            imageRes = null,
            gradientStart = Color(0xFFFACC15),
            gradientEnd = Color(0xFFEA580C),
            bannerTint = Color(0xFFFFF6D6),
            emoji = "😂",
            trending = true
        ),
        ServiceCategoryUi(
            id = "travel",
            name = "Travel",
            subtitle = "Trending hashtags",
            description = "Wanderlust hashtags, destination captions, and adventure story templates.",
            imageRes = null,
            gradientStart = Color(0xFF0EA5E9),
            gradientEnd = Color(0xFF1D4ED8),
            bannerTint = Color(0xFFE1F3FE),
            emoji = "✈️",
            trending = false
        ),
        ServiceCategoryUi(
            id = "fitness",
            name = "Fitness",
            subtitle = "Trending hashtags",
            description = "Gym motivation tags, workout captions, and transformation reel hooks.",
            imageRes = null,
            gradientStart = Color(0xFF10B981),
            gradientEnd = Color(0xFF047857),
            bannerTint = Color(0xFFE3F7EF),
            emoji = "💪",
            trending = true
        ),
        ServiceCategoryUi(
            id = "beauty",
            name = "Beauty",
            subtitle = "Trending hashtags",
            description = "Skincare routines, makeup tutorial tags, and glam captions.",
            imageRes = null,
            gradientStart = Color(0xFFEC4899),
            gradientEnd = Color(0xFF9D174D),
            bannerTint = Color(0xFFFCE9EF),
            emoji = "💄",
            trending = false
        ),
        ServiceCategoryUi(
            id = "gaming",
            name = "Gaming",
            subtitle = "Trending hashtags",
            description = "Esports highlights, stream bio templates, and viral gaming clip tags.",
            imageRes = null,
            gradientStart = Color(0xFF6366F1),
            gradientEnd = Color(0xFF312E81),
            bannerTint = Color(0xFFEAEBFF),
            emoji = "🎮",
            trending = true
        ),
        ServiceCategoryUi(
            id = "technology",
            name = "Technology",
            subtitle = "Trending hashtags",
            description = "Gadget unboxing tags, coding reels, and tech reviewer captions.",
            imageRes = null,
            gradientStart = Color(0xFF06B6D4),
            gradientEnd = Color(0xFF155E75),
            bannerTint = Color(0xFFE0F7FA),
            emoji = "💻",
            trending = false
        ),
        ServiceCategoryUi(
            id = "motivation",
            name = "Motivation",
            subtitle = "Trending hashtags",
            description = "Mindset quotes, daily hustle hashtags, and inspiring creator bios.",
            imageRes = null,
            gradientStart = Color(0xFFF97316),
            gradientEnd = Color(0xFFB91C1C),
            bannerTint = Color(0xFFFFEDE5),
            emoji = "🔥",
            trending = false
        ),
        ServiceCategoryUi(
            id = "lifestyle",
            name = "Lifestyle",
            subtitle = "Trending hashtags",
            description = "Aesthetic daily vlog tags, morning routine captions, and clean bios.",
            imageRes = null,
            gradientStart = Color(0xFF14B8A6),
            gradientEnd = Color(0xFF0F766E),
            bannerTint = Color(0xFFE0F5F2),
            emoji = "🌿",
            trending = false
        )
    )

    val defaultCoinPackages: List<CoinPackageItem> = listOf(
        CoinPackageItem(
            id = "pkg_1000",
            name = "1,000 Coins",
            coins = 1000L,
            bonusCoins = 250L,
            points = 3L,
            priceInr = 429L,
            badge = "Most Popular",
            sortOrder = 1L,
            active = true
        ),
        CoinPackageItem(
            id = "pkg_5000",
            name = "5,000 Coins",
            coins = 5000L,
            bonusCoins = 1000L,
            points = 15L,
            priceInr = 1699L,
            badge = "Best Value",
            sortOrder = 2L,
            active = true
        ),
        CoinPackageItem(
            id = "pkg_500",
            name = "500 Coins",
            coins = 500L,
            bonusCoins = 0L,
            points = 2L,
            priceInr = 249L,
            badge = "",
            sortOrder = 3L,
            active = true
        ),
        CoinPackageItem(
            id = "pkg_10000",
            name = "10,000 Coins",
            coins = 10000L,
            bonusCoins = 2500L,
            points = 30L,
            priceInr = 2999L,
            badge = "Super Saver",
            sortOrder = 4L,
            active = true
        )
    )

    val defaultServices: List<CatalogService> = listOf(
        // Christmas (matches uploaded screenshots!)
        CatalogService(
            id = "christmas_30",
            categoryId = "christmas",
            categoryName = "Christmas",
            name = "30 Christmas",
            packType = "HASHTAGS",
            itemCount = 30L,
            coinPrice = 3L,
            trending = true,
            published = true,
            contentLines = listOf(
                "#merrychristmas", "#christmastree", "#christmastime", "#christmasdecor",
                "#christmaseve", "#christmasiscoming", "#christmasgifts", "#christmaslights",
                "#christmasdecorations", "#christmasgift", "#christmasparty", "#christmasmood",
                "#christmasnails", "#christmasspirit", "#christmasmagic", "#christmasvibes",
                "#christmasshopping", "#christmasday", "#christmasmarket", "#christmascookies",
                "#happychristmas", "#christmaspresent", "#christmascountdown", "#firstchristmas",
                "#whitechristmas", "#winterwonderland", "#holidayseason", "#festivevibes",
                "#cozychristmas", "#santa"
            )
        ),
        CatalogService(
            id = "christmas_captions_15",
            categoryId = "christmas",
            categoryName = "Christmas",
            name = "15 Cozy Holiday Captions",
            packType = "CAPTIONS",
            itemCount = 15L,
            coinPrice = 5L,
            trending = false,
            published = true,
            contentLines = listOf(
                "Hot cocoa, twinkling lights, and endless holiday magic.",
                "Making spirits bright one cozy evening at a time.",
                "All I want for Christmas is good vibes and warm sweaters.",
                "Counting down the days till sleigh bells and snow.",
                "Deck the halls and keep the playlists rolling.",
                "Sweater weather just hit peak festive mode.",
                "Wrapped in fairy lights and winter gratitude.",
                "Santa's favorite creator checking in early this year.",
                "Cinnamon, pine needles, and memories in the making.",
                "Keep calm and jingle all the way.",
                "Dear Santa, define 'nice' before checking the list.",
                "Sparkle season is officially unlocked.",
                "Baking cookies and spreading cheer all December long.",
                "Home is wherever the Christmas tree is glowing.",
                "Wishing you joy, warmth, and viral holiday reels!"
            )
        ),
        // Food
        CatalogService(
            id = "food_30",
            categoryId = "food",
            categoryName = "Food",
            name = "30 Food",
            packType = "HASHTAGS",
            itemCount = 30L,
            coinPrice = 3L,
            trending = true,
            published = true,
            contentLines = listOf(
                "#foodie", "#foodporn", "#instafood", "#foodphotography", "#yummy",
                "#delicious", "#foodstagram", "#foodlover", "#homemade", "#foodblogger",
                "#streetfood", "#tasty", "#dinner", "#lunch", "#breakfast", "#foodgasm",
                "#healthyfood", "#cooking", "#chefmode", "#burgerlover", "#desserttime",
                "#indianfood", "#cafeVibes", "#recipeoftheday", "#eats", "#cravings",
                "#foodreels", "#comfortfood", "#flavorburst", "#hungry"
            )
        ),
        CatalogService(
            id = "food_captions_12",
            categoryId = "food",
            categoryName = "Food",
            name = "12 Foodie Captions",
            packType = "CAPTIONS",
            itemCount = 12L,
            coinPrice = 4L,
            trending = false,
            published = true,
            contentLines = listOf(
                "Good food is always a good mood.",
                "First we eat, then we do everything else.",
                "Calories don't count when the lighting is this good.",
                "Serving main-character flavor on every plate.",
                "Happiness is crispy fries and extra cheese.",
                "Save room for dessert — always.",
                "Warning: this reel may cause instant cravings.",
                "Made from scratch with a whole lot of spice.",
                "Weekend plans: eat, explore, repeat.",
                "Love at first bite.",
                "Street food hits different after sunset.",
                "Life is short, order the extra sauce."
            )
        ),
        // Fashion
        CatalogService(
            id = "fashion_30",
            categoryId = "fashion",
            categoryName = "Fashion",
            name = "30 Fashion",
            packType = "HASHTAGS",
            itemCount = 30L,
            coinPrice = 3L,
            trending = true,
            published = true,
            contentLines = listOf(
                "#fashion", "#style", "#ootd", "#fashionblogger", "#streetstyle",
                "#fashionista", "#instafashion", "#outfitoftheday", "#styleinspo",
                "#lookbook", "#trendy", "#fashionstyle", "#minimalstyle", "#luxuryfashion",
                "#vintagevibes", "#stylediary", "#editorialfashion", "#wardrobegoals",
                "#aestheticoutfit", "#runwayready", "#chic", "#fashionreels", "#desifashion",
                "#casualchic", "#menswear", "#womensfashion", "#accessories", "#sneakerhead",
                "#glam", "#dressup"
            )
        ),
        CatalogService(
            id = "fashion_bios_10",
            categoryId = "fashion",
            categoryName = "Fashion",
            name = "10 Style Creator Bios",
            packType = "BIOS",
            itemCount = 10L,
            coinPrice = 5L,
            trending = false,
            published = true,
            contentLines = listOf(
                "Curating effortless outfits & everyday luxury | Collabs via DM",
                "Streetwear, silhouettes & visual storytelling",
                "Turning sidewalks into runways one look at a time",
                "Minimal wardrobe, maximum confidence | Lookbooks weekly",
                "Your daily dose of color palettes & OOTD inspo",
                "Timeless basics meet bold statement pieces",
                "Styling moodboards for modern creators",
                "Chasing golden hour in my favorite fits",
                "Affordable glam & honest wardrobe edits",
                "Fashion is art you can wear every single day"
            )
        ),
        // Photography
        CatalogService(
            id = "photography_30",
            categoryId = "photography",
            categoryName = "Photography",
            name = "30 Photography",
            packType = "HASHTAGS",
            itemCount = 30L,
            coinPrice = 3L,
            trending = true,
            published = true,
            contentLines = listOf(
                "#photography", "#photooftheday", "#photographer", "#portraitphotography",
                "#streetphotography", "#goldenhour", "#lightroom", "#shotonmobile",
                "#visualstorytelling", "#moodygrams", "#dslr", "#lensculture",
                "#composition", "#bokeh", "#candid", "#landscape", "#urbanphotography",
                "#creativeportraits", "#editorial", "#colorgrading", "#shutterbug",
                "#rawphotos", "#filmvibes", "#nightphotography", "#minimalphotography",
                "#cinematic", "#aperture", "#framed", "#artofvisuals", "#focus"
            )
        ),
        // Funny
        CatalogService(
            id = "funny_30",
            categoryId = "funny",
            categoryName = "Funny",
            name = "30 Funny & Memes",
            packType = "HASHTAGS",
            itemCount = 30L,
            coinPrice = 3L,
            trending = true,
            published = true,
            contentLines = listOf(
                "#funny", "#memes", "#comedy", "#lol", "#relatable", "#funnymemes",
                "#humor", "#memeoftheday", "#laughoutloud", "#comedyreels", "#jokes",
                "#dailyhumor", "#sarcasm", "#viralmemes", "#funnyposts", "#hilarious",
                "#dankmemes", "#trendingreels", "#skit", "#povcomedy", "#goodlaughs",
                "#weekendhumor", "#desimemes", "#memepage", "#funnyvideos", "#mood",
                "#rofl", "#chuckle", "#justforfun", "#lolmoments"
            )
        ),
        // Travel
        CatalogService(
            id = "travel_30",
            categoryId = "travel",
            categoryName = "Travel",
            name = "30 Travel",
            packType = "HASHTAGS",
            itemCount = 30L,
            coinPrice = 3L,
            trending = true,
            published = true,
            contentLines = listOf(
                "#travel", "#wanderlust", "#travelgram", "#explore", "#adventure",
                "#travelphotography", "#vacation", "#roamtheplanet", "#solotravel",
                "#roadtrip", "#mountains", "#beachvibes", "#passportready", "#hiddengems",
                "#incredibleindia", "#travelreels", "#bucketlist", "#sunsetlovers",
                "#digitalnomad", "#weekendgetaway", "#globetrotter", "#scenic",
                "#naturelovers", "#backpacking", "#cityscape", "#traveldiaries",
                "#jetset", "#staycation", "#worldexplorer", "#discover"
            )
        ),
        // Fitness
        CatalogService(
            id = "fitness_25",
            categoryId = "fitness",
            categoryName = "Fitness",
            name = "25 Gym & Fitness",
            packType = "HASHTAGS",
            itemCount = 25L,
            coinPrice = 3L,
            trending = true,
            published = true,
            contentLines = listOf(
                "#fitness", "#gym", "#workout", "#fitfam", "#training", "#healthylifestyle",
                "#fitnessmotivation", "#bodybuilding", "#cardio", "#strength", "#gymlife",
                "#homeworkout", "#yoga", "#wellness", "#gains", "#noexcuses", "#fitlife",
                "#personaltrainer", "#running", "#crossfit", "#activewear", "#mindbody",
                "#discipline", "#sweat", "#fitnessreels"
            )
        ),
        // Beauty
        CatalogService(
            id = "beauty_25",
            categoryId = "beauty",
            categoryName = "Beauty",
            name = "25 Glam & Skincare",
            packType = "HASHTAGS",
            itemCount = 25L,
            coinPrice = 4L,
            trending = false,
            published = true,
            contentLines = listOf(
                "#beauty", "#skincare", "#makeup", "#glowup", "#makeuptutorial",
                "#selfcare", "#skincareroutine", "#glammakeup", "#cleanbeauty",
                "#beautyblogger", "#cosmetics", "#glowingskin", "#haircare", "#nailart",
                "#grwm", "#naturalglow", "#lipstick", "#softglam", "#beautytips",
                "#hydration", "#dewy", "#mua", "#beautyreels", "#everydaymakeup", "#radiant"
            )
        ),
        // Gaming
        CatalogService(
            id = "gaming_25",
            categoryId = "gaming",
            categoryName = "Gaming",
            name = "25 Gaming & Clips",
            packType = "HASHTAGS",
            itemCount = 25L,
            coinPrice = 3L,
            trending = true,
            published = true,
            contentLines = listOf(
                "#gaming", "#gamer", "#esports", "#gamingcommunity", "#pcgaming",
                "#mobilegaming", "#bgmi", "#valorant", "#streamer", "#gameplay",
                "#clutch", "#gamingclips", "#proplayer", "#rgbsetup", "#twitch",
                "#youtubegaming", "#gamingsetup", "#fps", "#battleroyale", "#indiegames",
                "#consolegaming", "#headshot", "#squadgoals", "#gg", "#gamingreels"
            )
        ),
        // Technology
        CatalogService(
            id = "tech_25",
            categoryId = "technology",
            categoryName = "Technology",
            name = "25 Tech & Gadgets",
            packType = "HASHTAGS",
            itemCount = 25L,
            coinPrice = 4L,
            trending = false,
            published = true,
            contentLines = listOf(
                "#tech", "#technology", "#gadgets", "#innovation", "#smartphone",
                "#desksetup", "#coding", "#developer", "#aitools", "#techreview",
                "#unboxing", "#minimaldesk", "#android", "#cybersecurity", "#futuretech",
                "#software", "#productivity", "#techtips", "#wearables", "#startuplife",
                "#robotics", "#appdev", "#techreels", "#workspace", "#digitalcreator"
            )
        ),
        // Motivation
        CatalogService(
            id = "motivation_20",
            categoryId = "motivation",
            categoryName = "Motivation",
            name = "20 Daily Hustle Tags",
            packType = "HASHTAGS",
            itemCount = 20L,
            coinPrice = 2L,
            trending = false,
            published = true,
            contentLines = listOf(
                "#motivation", "#mindset", "#success", "#hustle", "#growthmindset",
                "#selfimprovement", "#consistency", "#goals", "#focus", "#entrepreneur",
                "#dailyinspiration", "#positivity", "#nevergiveup", "#discipline",
                "#productivity", "#morningroutine", "#ambition", "#creatorlife",
                "#levelup", "#believe"
            )
        ),
        // Lifestyle
        CatalogService(
            id = "lifestyle_25",
            categoryId = "lifestyle",
            categoryName = "Lifestyle",
            name = "25 Aesthetic Lifestyle",
            packType = "HASHTAGS",
            itemCount = 25L,
            coinPrice = 3L,
            trending = false,
            published = true,
            contentLines = listOf(
                "#lifestyle", "#aesthetic", "#dailyvlog", "#minivlog", "#slowliving",
                "#cozyvibes", "#morningcoffee", "#homedecor", "#selflove", "#goodvibes",
                "#weekendvibes", "#aestheticreels", "#journaling", "#mindfulness",
                "#simplicity", "#dayinmylife", "#wellness", "#creative", "#inspo",
                "#minimalism", "#sunlight", "#calm", "#vibe", "#everydaymagic", "#creator"
            )
        )
    )
}
