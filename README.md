# sparklin
A Restful HTTP Framework built on top of Spark making it as easy to use as NodeJS's Express or Ruby's Sinatra

# Example
```kotlin
data class ErrorBean(val code: String, val message: String? = null)
data class TestBean(val name: String, val age: Int, val list: List<String>, val ts: Date = Date())

object OptionalAuthorizationManager : AuthorizationManager {
    override fun getAuthz(request: Request): Collection<String> {
        return listOf("IAMME")
    }
}

fun main(args: Array<String>) {
    val bean = TestBean("Test Stuff", 42, listOf("Bob", "Frank"))

    Sparklin(SparklinConfig(authorizationManager = OptionalAuthorizationManager)) {
        before { req, res -> res.type("application/json") }
        before("/silly") { req, res ->
            res.type("application/override")
            res.header("Purple", "fiddlestix")
        }

        exception(Exception::class) { e, req, res ->
            println("Serious exception happened here: ${e.message}")
            Pair(500, listOf(ErrorBean("fubar", e.message), ErrorBean("snafu", "Just another for fun")))
        }

        get("/hello") { req, res -> bean }

        post("/hello") { req, res ->
            hasAny("ANONYMOUS", "IAMME").required("name").date("ts").stopOnRejections()
            val play = req.jsonAs<TestBean>()
            play.age *= 2
            play
        }

        get("/noauth") { req, res ->
            hasAll("NO SUCH PERM")
        }

        get("/error") { req, res ->
            res.status(401) // will be changed to 500 by exception
            res.body("Fiddle") // will be changed to error message instead
            throw NullPointerException("Mommy please save me")
        }
    }
}
```
