package com.hgy.flutter.zd.flutter_zendes_plugin

import android.app.Activity
import android.text.TextUtils
import androidx.annotation.NonNull
import com.zendesk.logger.Logger
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import zendesk.chat.*
import zendesk.configurations.Configuration
import zendesk.core.AnonymousIdentity
import zendesk.core.Identity
import zendesk.core.Zendesk
import zendesk.messaging.MessagingActivity
import zendesk.support.Support
import zendesk.support.guide.HelpCenterActivity
import zendesk.support.request.RequestActivity
import zendesk.support.requestlist.RequestListActivity
import java.lang.Exception


public class FlutterZendesPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
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
                // Set logger on debug
                Logger.setLoggable(BuildConfig.DEBUG)

                // Get variables from flutter
                val accountKey = call.argument<String>("accountKey") ?: ""
                val applicationId = call.argument<String>("applicationId") ?: ""
                val clientId = call.argument<String>("clientId") ?: ""
                val domainUrl = call.argument<String>("domainUrl") ?: ""

                if (TextUtils.isEmpty(accountKey)) {
                    result.error("ACCOUNT_KEY_NULL", "AccountKey is null !", "AccountKey is null !")
                    return
                }


                try {
                    // 1.Init Zendesk SDK instance
                    Zendesk.INSTANCE.init(activity,
                            domainUrl,
                            applicationId,
                            clientId)
                    // 2.Init Support SDK instance
                    Support.INSTANCE.init(Zendesk.INSTANCE)

                    // 3.Setting Anonymous identity for Zendesk SDK
                    val identity: Identity = AnonymousIdentity.Builder()
                            .build()
                    Zendesk.INSTANCE.setIdentity(identity)

                    // 4.Init ChatV2 SDK instance
                    Chat.INSTANCE.init(activity, accountKey, applicationId)

                    result.success("Init completed!")
                } catch (e: Exception) {
                    result.error("INIT_EXCEPTION", e.message, e)
                }
            }
            "startChatV2" -> {

                val phone = call.argument<String>("phone") ?: ""
                val email = call.argument<String>("email") ?: ""
                val name = call.argument<String>("name") ?: ""

                val botLabel = call.argument<String>("botLabel")
                val toolbarTitle = call.argument<String>("toolbarTitle")
                val endChatSwitch = call.argument<Boolean>("endChatSwitch") ?: true
                val departmentName = call.argument<String>("departmentName") ?: "Department name"
                val botAvatar = call.argument<Int>("botAvatar") ?: R.drawable.zui_avatar_bot_default
                val profileProvider = Chat.INSTANCE.providers()?.profileProvider()
                val chatProvider = Chat.INSTANCE.providers()?.chatProvider()

                var isPre = false;
                if (TextUtils.isEmpty(phone)) {
                    isPre = true;
                }
                val visitorInfo = VisitorInfo.builder().withName(name).withEmail(email).withPhoneNumber(phone).build()
                profileProvider?.setVisitorInfo(visitorInfo, null)
                profileProvider?.setVisitorNote("Name : $name ; Phone: $phone", null)
                chatProvider?.setDepartment(departmentName, null)
                val chatConfigurationBuilder = ChatConfiguration.builder();
                chatConfigurationBuilder
                        //If true, and no agents are available to serve the visitor, they will be presented with a message letting them know that no agents are available. If it's disabled, visitors will remain in a queue waiting for an agent. Defaults to true.
                        .withAgentAvailabilityEnabled(true)
                        //If true, visitors will be prompted at the end of the chat if they wish to receive a chat transcript or not. Defaults to true.
                        .withTranscriptEnabled(true)
                        .withOfflineFormEnabled(true)
                        //If true, visitors are prompted for information in a conversational manner prior to starting the chat. Defaults to true.
                        .withPreChatFormEnabled(isPre)
                        .withNameFieldStatus(PreChatFormFieldStatus.HIDDEN)
                        .withEmailFieldStatus(PreChatFormFieldStatus.HIDDEN)
                        .withPhoneFieldStatus(PreChatFormFieldStatus.REQUIRED)
                        .withDepartmentFieldStatus(PreChatFormFieldStatus.OPTIONAL)
                if (!endChatSwitch) {
                    chatConfigurationBuilder.withChatMenuActions(ChatMenuAction.CHAT_TRANSCRIPT)
                }
                val chatConfiguration = chatConfigurationBuilder.build();

                MessagingActivity.builder()
                        .withBotLabelString(botLabel)
                        .withBotAvatarDrawable(botAvatar)
                        .withToolbarTitle(toolbarTitle)
                        .withEngines(ChatEngine.engine())
                        .show(activity, chatConfiguration)

            }
            "helpCenter" -> {
                // Get variables from flutter
                val categoriesCollapsed = call.argument<Boolean>("categoriesCollapsed") ?: false
                val contactUsButtonVisible = call.argument<Boolean>("contactUsButtonVisible") ?: true
                val showConversationsMenuButton = call.argument<Boolean>("showConversationsMenuButton") ?: true

                // Set configuration
                val helpCenterConfig: Configuration = HelpCenterActivity.builder()
                        .withCategoriesCollapsed(categoriesCollapsed)
                        .withContactUsButtonVisible(contactUsButtonVisible)
                        .withShowConversationsMenuButton(showConversationsMenuButton)
                        .config()

                try {
                    // Start HelpCenter
                    HelpCenterActivity.builder()
                            .show(activity, helpCenterConfig)

                    result.success("Started HelpCenter")
                } catch (e: Exception) {
                    result.error("CALL_HELPCENTER", e.message, e)
                }
            }
            "requestView" -> {
                RequestActivity.builder()
                        .show(activity);
            }
            "requestListView" -> {
                RequestListActivity.builder()
                        .show(activity);
            }
            "changeNavStatus" -> {

            }
            else -> {
                result.notImplemented()
            }
        }
    }
}
