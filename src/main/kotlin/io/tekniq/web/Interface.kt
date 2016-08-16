package io.tekniq.web

import spark.Request
import spark.Response
import spark.ResponseTransformer
import spark.utils.SparkUtils
import kotlin.reflect.KClass

class NotAuthorizedException(rejections: Collection<Rejection>) : ValidationException(rejections)

interface AuthorizationManager {
    /**
     * Must return an empty list if no access is to be granted. Best practice says to return AUTHENTICATED if the user
     * is authenticated and to return ANONYMOUS if the user is not authenticated.
     */
    fun getAuthz(request: Request): Collection<String>
}

interface SparklinRoute {
    fun before(path: String = SparkUtils.ALL_PATHS, acceptType: String = "*/*", filter: SparklinValidation.(Request, Response) -> Unit)
    fun after(path: String = SparkUtils.ALL_PATHS, acceptType: String = "*/*", filter: SparklinValidation.(Request, Response) -> Unit)
    fun get(path: String, acceptType: String = "*/*", transformer: ResponseTransformer? = null, route: SparklinValidation.(Request, Response) -> Any?)
    fun post(path: String, acceptType: String = "*/*", transformer: ResponseTransformer? = null, route: SparklinValidation.(Request, Response) -> Any?)
    fun put(path: String, acceptType: String = "*/*", transformer: ResponseTransformer? = null, route: SparklinValidation.(Request, Response) -> Any?)
    fun patch(path: String, acceptType: String = "*/*", transformer: ResponseTransformer? = null, route: SparklinValidation.(Request, Response) -> Any?)
    fun delete(path: String, acceptType: String = "*/*", transformer: ResponseTransformer? = null, route: SparklinValidation.(Request, Response) -> Any?)
    fun head(path: String, acceptType: String = "*/*", transformer: ResponseTransformer? = null, route: SparklinValidation.(Request, Response) -> Any?)
    fun trace(path: String, acceptType: String = "*/*", transformer: ResponseTransformer? = null, route: SparklinValidation.(Request, Response) -> Any?)
    fun connect(path: String, acceptType: String = "*/*", transformer: ResponseTransformer? = null, route: SparklinValidation.(Request, Response) -> Any?)
    fun options(path: String, acceptType: String = "*/*", transformer: ResponseTransformer? = null, route: SparklinValidation.(Request, Response) -> Any?)
    fun webSocket(path: String, handler: KClass<*>)

    fun <T : Exception> exception(exceptionClass: KClass<T>, handler: (T, Request, Response) -> Pair<Int, Any>)
}

fun <T : Any> Request.jsonAs(type: KClass<T>): T? {
    if (this.body() == "") {
        return null
    }
    return mapper.readValue(this.body(), type.java)
}

inline fun <reified T : Any> Request.jsonAs(): T? = jsonAs(T::class)

data class SparklinConfig(
        val ip: String = "0.0.0.0", val port: Int = 4567,
        val authorizationManager: AuthorizationManager? = null,
        val responseTransformer: ResponseTransformer = JsonResponseTransformer,
        val webSocketTimeout: Int? = null,
        val maxThreads: Int = 10, val minThreads: Int = -1, val idleTimeoutMillis: Int = -1,
        val keystore: SparklinKeystore? = null,
        val staticFiles: SparklinStaticFiles? = null)

data class SparklinKeystore(val keystoreFile: String, val keystorePassword: String,
                            val truststoreFile: String, val truststorePassword: String)

data class SparklinStaticFiles(val fileLocation: String? = null, val externalFileLocation: String? = null,
                               val headers: Map<String, String> = emptyMap(), val expireInSeconds: Int = 1)

private object JsonResponseTransformer : ResponseTransformer {
    override fun render(model: Any?): String = mapper.writeValueAsString(model)
}
