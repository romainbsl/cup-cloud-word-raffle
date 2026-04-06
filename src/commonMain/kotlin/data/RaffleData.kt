package data

/**
 * Static data used by the raffle word-cloud slide.
 *
 * [participants] is the pool of names that can be drawn.
 * [words] is a set of Kotlin-ecosystem keywords displayed in the background cloud.
 */
object RaffleData {
    /** Conference attendees / speakers eligible for the draw. */
    val participants: List<String> = listOf(
        "Adele Carpenter", "Akniyet Arysbayev", "Alejandro Serrano Mena", "Aleksei Zinovev",
        "Alessio Della Motta", "Anastasiia Birillo", "Andrey Bragin", "Anna Kozlova",
        "Annunziata Kinya", "Anton Arhipov", "Arnaud Giuliani", "Artem Kobzar",
        "Aurimas Liutikas", "Baruch Sadogursky", "Ben Kadel", "Bernd Prünster",
        "Bowen Feng", "Brian Norman", "Chantal Loncle", "Christian Ryan",
        "Dan Kim", "Daniel Santiago Rivera", "Daniil Karol", "David Denton",
        "Denis Ambatenne", "Duncan McGregor", "Elif Bilgin Morris", "Eric Kuck",
        "Filipp Zhinkin", "Gabriele Pappalardo", "Gleb Lukianets", "Hadi Hariri",
        "Hammad Akram", "Huyen Tue Dao", "Ian Botsford", "Ian Lake",
        "Ian Leshan", "Idan Nakav", "Jake Wharton", "Jeffrey van Gogh",
        "Jessalyn Wang", "Jesse Wilson", "Joffrey Bion", "John O'Reilly",
        "Jonathan Schneider", "Josh Long", "Konstantin Tskhovrebov", "Ksenia Shneyveys",
        "Lauren Darcey", "Lena Reinhard", "Marat Akhin", "Marc Reichelt",
        "Marcello Galhardo", "Marcin Mycek", "Maria Krishtal", "Márton Braun",
        "Meike Felicia Hammer", "Merlin Pahic", "Michail Zarečenskij", "Michal Harakal",
        "Mohamed Ben Rejeb", "Nat Pryce", "Natalia Mishina", "Natasha Murashkina",
        "Nicole Terc", "Oliver Okrongli", "Olivier Notteghem", "Omico Wang",
        "Pamela Hill", "Paul Merlin", "Phil Burk", "Rahul Behera",
        "Rooz SF", "Ross Tate", "Ryan Ulep", "Salomon Brys",
        "Sam Berlin", "Sam Gammon", "Sebastian Aigner", "Sébastien Deleuze",
        "Sergei Rybalkin", "Sergio Carrilho", "Simon Vergauwen", "Sinan Kozak",
        "Stanislav Sandler", "Stefan Wolf", "Suhyeon Kim", "Svetlana Isakova",
        "Tadeas Kriz", "Tim Schraepen", "Timofey Solonin", "Tunji Dahunsi",
        "Ty Smith", "Urs Peter", "Vadim Briliantov", "Victor Kropp",
        "Viktor Gamov", "Vsevolod Tolstopyatov", "Wojtek Kalicinski", "Yuri Geronimus",
        "Yuri Schimke", "Zac Sweers", "Zalim Bashorov",
    )

    /** Kotlin-ecosystem buzzwords rendered as ambient word-cloud filler. */
    val words: List<String> = listOf(
        "Coroutines", "Flow", "StateFlow", "expect/actual", "Compose",
        "KMP", "Gradle", "Multiplatform", "suspend", "sealed",
        "inline", "Koin", "Ktor", "SQLDelight", "Kodein-DI",
        "Wasm", "iOS", "Android", "Desktop", "Channels",
        "Sharing", "Native", "Cinterop", "Serialization", "Koog",
        "Amper", "Fleet", "Junie", "Arrow", "Result",
        "Delegates", "DSL", "Reified", "Inline class", "Value class",
        "Context receivers", "K2", "KSP", "Compose compiler", "Lazy"
    )
}
