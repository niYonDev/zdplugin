package com.hgy.flutter.zd.flutter_zendes_plugin

//import com.zopim.android.sdk.api.ZopimChat
//import com.zopim.android.sdk.model.VisitorInfo
//import com.zopim.android.sdk.prechat.PreChatForm
//import com.zopim.android.sdk.prechat.ZopimChatActivity
import android.app.Activity
import android.text.TextUtils
import androidx.annotation.NonNull
import com.zendesk.service.ErrorResponse
import com.zendesk.service.ZendeskCallback
import com.zendesk.util.ObjectUtils
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import zendesk.answerbot.AnswerBot
import zendesk.answerbot.AnswerBotEngine
import zendesk.chat.*
import zendesk.configurations.Configuration
import zendesk.core.AnonymousIdentity
import zendesk.core.Identity
import zendesk.core.Zendesk
import zendesk.messaging.MessagingActivity
import zendesk.messaging.ui.MessagingView
import zendesk.support.Support
import zendesk.support.SupportEngine
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
                val zendeskUrl = call.argument<String>("domainUrl") ?: ""
                val nameIdentifier = call.argument<String>("nameIdentifier") ?: "nameIdentifier"
                val emailIdentifier = call.argument<String>("emailIdentifier") ?: "emailIdentifier"
                if (TextUtils.isEmpty(accountKey)) {
                    result.error("ACCOUNT_KEY_NULL", "AccountKey is null !", "AccountKey is null !")
                }
                //1.Zendes SDK
                Zendesk.INSTANCE.init(activity,
                        zendeskUrl,
                        applicationId,
                        clientId)
                //2.Support SDK init
                Support.INSTANCE.init(Zendesk.INSTANCE)
                //3.setIdentity
                Zendesk.INSTANCE.setIdentity(
                        AnonymousIdentity.Builder()
                                .withNameIdentifier(nameIdentifier)
                                .withEmailIdentifier(emailIdentifier)
                                .build()
                )
                //4.Chat SDK
                Chat.INSTANCE.init(activity, accountKey)
                //5.AnswerBot SDK
                AnswerBot.INSTANCE.init(Zendesk.INSTANCE, Support.INSTANCE)
                result.success("Init completed!")
            }
            "startChat" -> {
                val phone = call.argument<String>("phone") ?: ""
                val email = call.argument<String>("email") ?: ""
                val name = call.argument<String>("name") ?: ""
                val botLabel = call.argument<String>("botLabel")
                val toolbarTitle = call.argument<String>("toolbarTitle")
                val botAvatar = call.argument<Int>("botAvatar") ?: R.drawable.zui_avatar_bot_default
                val visitorInfo = VisitorInfo.builder().withName(name).withEmail(email).withPhoneNumber(phone).build()
                val chatConfiguration = ChatConfiguration.builder()
                        //If true, and no agents are available to serve the visitor, they will be presented with a message letting them know that no agents are available. If it's disabled, visitors will remain in a queue waiting for an agent. Defaults to true.
                        .withAgentAvailabilityEnabled(true)
                        //If true, visitors will be prompted at the end of the chat if they wish to receive a chat transcript or not. Defaults to true.
                        .withTranscriptEnabled(true)
                        .withOfflineFormEnabled(true)
                        //If true, visitors are prompted for information in a conversational manner prior to starting the chat. Defaults to true.
                        .withPreChatFormEnabled(true)
                        .withNameFieldStatus(PreChatFormFieldStatus.OPTIONAL)
                        .withEmailFieldStatus(PreChatFormFieldStatus.OPTIONAL)
                        .withPhoneFieldStatus(PreChatFormFieldStatus.OPTIONAL)
                        .withDepartmentFieldStatus(PreChatFormFieldStatus.OPTIONAL)
                        .build()
                MessagingActivity.builder()
                        .withBotLabelString(botLabel)
                        .withBotAvatarDrawable(botAvatar)
                        .withToolbarTitle(toolbarTitle)
                        .withEngines(ChatEngine.engine(), AnswerBotEngine.engine(), SupportEngine.engine())
                        .show(activity, chatConfiguration)

            }
            "helpCenter" -> {
                val categoriesCollapsed = call.argument<Boolean>("categoriesCollapsed") ?: false
                val contactUsButtonVisible = call.argument<Boolean>("contactUsButtonVisible")
                        ?: true
                val showConversationsMenuButton = call.argument<Boolean>("showConversationsMenuButton")
                        ?: true
                val helpCenterConfig: Configuration = HelpCenterActivity.builder()
                        .withCategoriesCollapsed(categoriesCollapsed)
                        .withContactUsButtonVisible(contactUsButtonVisible)
                        .withShowConversationsMenuButton(showConversationsMenuButton)
                        .config()
                HelpCenterActivity.builder()
                        .show(activity, helpCenterConfig)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

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
//        val chatConfiguration = ChatConfiguration.builder().build()
//
////        context.startActivity(Intent(context, ZopimChatActivity::class.java))
////
////        // Sample breadcrumb
////        ZopimChat.trackEvent("Started chat without config")
//
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
