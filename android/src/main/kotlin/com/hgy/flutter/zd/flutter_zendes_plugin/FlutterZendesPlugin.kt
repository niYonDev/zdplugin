package com.hgy.flutter.zd.flutter_zendes_plugin

import android.app.Activity
import android.text.TextUtils
import androidx.annotation.NonNull
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
import zendesk.core.Zendesk
import zendesk.messaging.MessagingActivity
import zendesk.support.Support
import zendesk.support.SupportEngine
import zendesk.support.guide.HelpCenterActivity
import zendesk.support.request.RequestActivity
import zendesk.support.requestlist.RequestListActivity


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
            "startChatV2" -> {
                val phone = call.argument<String>("phone") ?: ""
                val email = call.argument<String>("email") ?: ""
                val name = call.argument<String>("name") ?: ""
                val botLabel = call.argument<String>("botLabel")
                val toolbarTitle = call.argument<String>("toolbarTitle")
                val departmentName = call.argument<String>("departmentName") ?: "Department name"
                val botAvatar = call.argument<Int>("botAvatar") ?: R.drawable.zui_avatar_bot_default

                val profileProvider = Chat.INSTANCE.providers()?.profileProvider()
                val chatProvider = Chat.INSTANCE.providers()?.chatProvider()

                val visitorInfo = VisitorInfo.builder().withName(name).withEmail(email).withPhoneNumber(phone).build()
                profileProvider?.setVisitorInfo(visitorInfo, null)
                chatProvider?.setDepartment(departmentName, null)
                val chatConfiguration = ChatConfiguration.builder()
                        //If true, and no agents are available to serve the visitor, they will be presented with a message letting them know that no agents are available. If it's disabled, visitors will remain in a queue waiting for an agent. Defaults to true.
                        .withAgentAvailabilityEnabled(true)
                        //If true, visitors will be prompted at the end of the chat if they wish to receive a chat transcript or not. Defaults to true.
                        .withTranscriptEnabled(true)
                        .withOfflineFormEnabled(true)
                        //If true, visitors are prompted for information in a conversational manner prior to starting the chat. Defaults to true.
                        .withPreChatFormEnabled(true)
                        .withNameFieldStatus(PreChatFormFieldStatus.HIDDEN)
                        .withEmailFieldStatus(PreChatFormFieldStatus.HIDDEN)
                        .withPhoneFieldStatus(PreChatFormFieldStatus.REQUIRED)
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
            "requestView" -> {
                RequestActivity.builder()
                        .show(activity);
            }
            "requestListView" -> {
                RequestListActivity.builder()
                        .show(activity);
            }
            else -> {
                result.notImplemented()
            }
        }
    }
}
