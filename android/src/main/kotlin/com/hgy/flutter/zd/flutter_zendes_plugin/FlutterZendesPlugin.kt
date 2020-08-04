package com.hgy.flutter.zd.flutter_zendes_plugin

//import com.zopim.android.sdk.api.ZopimChat
//import com.zopim.android.sdk.model.VisitorInfo
//import com.zopim.android.sdk.prechat.PreChatForm
//import com.zopim.android.sdk.prechat.ZopimChatActivity
import android.app.Activity
import androidx.annotation.NonNull
import com.zendesk.util.ObjectUtils
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import zendesk.chat.Chat
import zendesk.chat.ChatConfiguration
import zendesk.chat.ChatEngine
import zendesk.configurations.Configuration
import zendesk.core.AnonymousIdentity
import zendesk.core.Identity
import zendesk.core.Zendesk
import zendesk.messaging.MessagingActivity
import zendesk.support.Support
import zendesk.support.guide.HelpCenterActivity


/** FlutterZendesPlugin */
public class FlutterZendesPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var activity: Activity

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "flutter_zendes_plugin")
        channel.setMethodCallHandler(this);
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "flutter_zendes_plugin")
            channel.setMethodCallHandler(FlutterZendesPlugin())
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "init" -> {
                val accountKey = call.argument<String>("accountKey") ?: ""
                val applicationId = call.argument<String>("applicationId") ?: ""
                val clientId = call.argument<String>("clientId") ?: ""
                val domainUrl = call.argument<String>("domainUrl") ?: ""
                if (ObjectUtils.checkNonNull(accountKey)) {
                    result.error("ACCOUNT_KEY_NULL", "AccountKey is null !", "AccountKey is null !")
                }
                /**
                 * Initialize the SDK with your Zendesk subdomain, mobile SDK app ID, and client ID.
                 *
                 * Get these details from your Zendesk dashboard: Admin -> Channels -> MobileSDK.
                 */
                Zendesk.INSTANCE.init(activity,
                        domainUrl,
                        applicationId,
                        clientId)

                /**
                 * Set an identity (authentication).
                 *
                 * Set either Anonymous or JWT identity, as below:
                 */

                // a). Anonymous (All fields are optional)
                /**
                 * Set an identity (authentication).
                 *
                 * Set either Anonymous or JWT identity, as below:
                 */

                // a). Anonymous (All fields are optional)
                Zendesk.INSTANCE.setIdentity(
                        AnonymousIdentity.Builder()
                                .withNameIdentifier("{optional name}")
                                .withEmailIdentifier("{optional email}")
                                .build()
                )

                // b). JWT (Must be initialized with your JWT identifier)

                // b). JWT (Must be initialized with your JWT identifier)
                val identity: Identity = AnonymousIdentity()
                Zendesk.INSTANCE.setIdentity(identity)

                Support.INSTANCE.init(Zendesk.INSTANCE)
                // Sample breadcrumb
//                ZopimChat.init(accountKey)
//                ZopimChat.trackEvent("Application Description ! Hello World!")

                Chat.INSTANCE.init(activity, accountKey)
                result.success("Init completed!")
            }
            "startChat" -> {
                val type = call.argument<Int>("type") ?: 0
                val phone = call.argument<String>("phone") ?: ""
                val email = call.argument<String>("email") ?: ""
                val name = call.argument<String>("name") ?: ""
                val chatConfiguration = ChatConfiguration.builder().build()

                MessagingActivity.builder()
                        .withEngines(ChatEngine.engine())
                        .show(activity, chatConfiguration)
//                when (type) {
//                    0 -> {
//                        startNoConfigChat(activity)
//                    }
//                    1 -> {
//                        startOptionalPreChat(activity)
//                    }
//                    2 -> {
//                        startNoConfigChat(activity)
//                    }
//                    3 -> {
//                        startNoPreChat(activity)
//                    }
//                    4 -> {
//                        startMandatoryPreChat(activity)
//                    }
//                    5 -> {
//                        startPreSetData(phone, email, name)
//                    }
//                }
            }
            "helpCenter" -> {
                val helpCenterConfig: Configuration = HelpCenterActivity.builder()
                        .withCategoriesCollapsed(false)
                        .withContactUsButtonVisible(true)
                        .withShowConversationsMenuButton(true)
                        .config()
                HelpCenterActivity.builder()
                        .show(activity, helpCenterConfig)
            }
            else -> {
                result.notImplemented()
            }
        }
    }
//
//    /**
//     * Pre-sets [com.zopim.android.sdk.model.VisitorInfo] data in the chat config and starts the new chat
//     */
//    private fun startPreSetData(phone: String, email: String, name: String) {
//        // build and set visitor info
//        val visitorInfo = VisitorInfo.Builder()
//                .phoneNumber(phone)
//                .email(email)
//                .name(name)
//                .build()
//
//        // visitor info can be set at any point when that information becomes available
//        ZopimChat.setVisitorInfo(visitorInfo)
//        // set pre chat fields as mandatory
//        val preChatForm = PreChatForm.Builder()
//                .name(PreChatForm.Field.REQUIRED_EDITABLE)
//                .email(PreChatForm.Field.REQUIRED_EDITABLE)
//                .phoneNumber(PreChatForm.Field.REQUIRED_EDITABLE)
//                .department(PreChatForm.Field.REQUIRED_EDITABLE)
//                .message(PreChatForm.Field.REQUIRED_EDITABLE)
//                .build()
//
//        // build chat config
//        val config = ZopimChat.SessionConfig().preChatForm(preChatForm).department("Department My memory")
//
//        // start chat activity with config
//        ZopimChatActivity.startActivity(activity, config)
//
//        // Sample breadcrumb
//        ZopimChat.trackEvent("Started chat with pre-set visitor information,~~~~")
//    }
//
//
//    /**
//     * Starts the chat with global config that was provided at init state via [com.zopim.android.sdk.api.ZopimChatApi.init]
//     *
//     * @see Global
//     */
//    private fun startNoConfigChat(context: Context) {
//        context.startActivity(Intent(context, ZopimChatActivity::class.java))
//
//        // Sample breadcrumb
//        ZopimChat.trackEvent("Started chat without config")
//    }
//
//    /**
//     * Starts the chat with all pre chat form fields set as [PreChatForm.Field.OPTIONAL] optional
//     */
//    private fun startOptionalPreChat(context: Context) {
//        // set pre chat fields as optional
//        val preChatConfig = PreChatForm.Builder()
//                .name(PreChatForm.Field.OPTIONAL_EDITABLE)
//                .email(PreChatForm.Field.OPTIONAL_EDITABLE)
//                .phoneNumber(PreChatForm.Field.OPTIONAL_EDITABLE)
//                .department(PreChatForm.Field.OPTIONAL_EDITABLE)
//                .message(PreChatForm.Field.OPTIONAL_EDITABLE)
//                .build()
//
//        // build chat config
//        val config = ZopimChat.SessionConfig().preChatForm(preChatConfig)
//
//        // start chat activity with config
//        ZopimChatActivity.startActivity(context, config)
//
//        // Sample breadcrumb
//        ZopimChat.trackEvent("Started chat with optional pre-chat form")
//    }
//
//    /**
//     * Starts the chat with all pre chat form fields set as [PreChatForm.Field.REQUIRED] mandatory
//     */
//    private fun startMandatoryPreChat(context: Context) {
//        // set pre chat fields as mandatory
//        val preChatForm = PreChatForm.Builder()
//                .name(PreChatForm.Field.REQUIRED_EDITABLE)
//                .email(PreChatForm.Field.REQUIRED_EDITABLE)
//                .phoneNumber(PreChatForm.Field.REQUIRED_EDITABLE)
//                .department(PreChatForm.Field.REQUIRED_EDITABLE)
//                .message(PreChatForm.Field.REQUIRED_EDITABLE)
//                .build()
//
//        // build chat config
//        val config = ZopimChat.SessionConfig().preChatForm(preChatForm)
//
//        // start chat activity with config
//        ZopimChatActivity.startActivity(context, config)
//
//        // Sample breadcrumb
//        ZopimChat.trackEvent("Started chat with mandatory pre-chat form")
//    }
//
//    /**
//     * Starts the chat all pre chat form fields set as [PreChatForm.Field.NOT_REQUIRED] hidden
//     */
//    private fun startNoPreChat(context: Context) {
//        // set pre chat fields as hidden
//        val preChatForm = PreChatForm.Builder()
//                .name(PreChatForm.Field.NOT_REQUIRED)
//                .email(PreChatForm.Field.NOT_REQUIRED)
//                .phoneNumber(PreChatForm.Field.NOT_REQUIRED)
//                .department(PreChatForm.Field.NOT_REQUIRED)
//                .message(PreChatForm.Field.NOT_REQUIRED)
//                .build()
//
//        // build chat config
//        val config = ZopimChat.SessionConfig().preChatForm(preChatForm)
//
//        // start chat activity with config
//        ZopimChatActivity.startActivity(context, config)
//
//        // Sample breadcrumb
//        ZopimChat.trackEvent("Started chat without pre-chat form")
//    }

}
