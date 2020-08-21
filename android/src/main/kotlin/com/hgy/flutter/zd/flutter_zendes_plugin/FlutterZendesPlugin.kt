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
import java.util.*
import kotlin.collections.HashMap


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

    private fun getPreChatFieldStatus(status: String?): PreChatFormFieldStatus {
        return when(status?.toUpperCase(Locale.getDefault())) {
            "REQUIRED" -> PreChatFormFieldStatus.REQUIRED
            "HIDDEN" -> PreChatFormFieldStatus.HIDDEN
            "OPTIONAL" -> PreChatFormFieldStatus.OPTIONAL
            else -> PreChatFormFieldStatus.REQUIRED
        }
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
                // Get Visitor variables
                val visitorPhone = call.argument<String>("visitorPhone") ?: ""
                val visitorEmail = call.argument<String>("visitorEmail") ?: ""
                val visitorName = call.argument<String>("visitorName") ?: ""
                val visitorTags =  call.argument<List<String>>("visitorTags") ?: emptyList()
                val visitorNotes = call.argument<String>("visitorNotes") ?: ""

                // Variables Chat features
                val departmentName = call.argument<String>("departmentName") ?: "Department name"
                val botLabel = call.argument<String>("botLabel")
                val botAvatar = call.argument<Int>("botAvatar") ?: R.drawable.zui_avatar_bot_default
                val toolbarTitle = call.argument<String>("toolbarTitle") ?: "Chat"
                val withAgentAvailabilityEnabled = call.argument<Boolean>("withAgentAvailabilityEnabled") ?: true
                val withChatTranscriptsEnabled = call.argument<Boolean>("withChatTranscriptsEnabled") ?: false
                val withPreChatFormEnabled = call.argument<Boolean>("withPreChatFormEnabled") ?: true
                val withPreChatFormOptions = call.argument<HashMap<String, String>>("withPreChatFormOptions") ?: HashMap<String,String>()
                val withOfflineFormsEnabled = call.argument<Boolean>("withOfflineFormsEnabled") ?: true
                val withChatMenuActions = call.argument<String>("withChatMenuActions")

                // Init Profile and Chat provider
                val profileProvider = Chat.INSTANCE.providers()?.profileProvider()
                val chatProvider = Chat.INSTANCE.providers()?.chatProvider()

                // Visitor and Chat Builders
                val visitorInfoBuilder = VisitorInfo.builder()
                val chatConfigurationBuilder = ChatConfiguration.builder();

                // Set visitor info builder
                if(visitorPhone.isNotEmpty())
                    visitorInfoBuilder.withPhoneNumber(visitorPhone)

                if(visitorEmail.isNotEmpty())
                    visitorInfoBuilder.withEmail(visitorEmail)

                if(visitorName.isNotEmpty())
                    visitorInfoBuilder.withName(visitorName)

                // Set profile provider
                profileProvider?.setVisitorInfo(visitorInfoBuilder.build(), null)

                if(visitorNotes.isNotEmpty())
                    profileProvider?.setVisitorNote(visitorNotes, null)

                if(visitorTags.isNotEmpty())
                    profileProvider?.addVisitorTags(visitorTags, null)

                // Set Chat provider parameters
                if(departmentName.isNotEmpty())
                    chatProvider?.setDepartment(departmentName, null)

                // Set Chat Configuration builder
                chatConfigurationBuilder
                        .withAgentAvailabilityEnabled(withAgentAvailabilityEnabled)
                        .withTranscriptEnabled(withChatTranscriptsEnabled)
                        .withPreChatFormEnabled(withPreChatFormEnabled)
                        .withOfflineFormEnabled(withOfflineFormsEnabled)
                        .withChatMenuActions()

                /**
                 *  Set all fields REQUIRED, if detects the parameter used in the HashMap withPreChatFormOptions,
                 *  get its current String value and convert it to PreChatFormFieldStatus,
                 *  then set it in ChatConfigurationBuilder
                 */
                if(withPreChatFormEnabled) {
                    chatConfigurationBuilder.withNameFieldStatus(PreChatFormFieldStatus.REQUIRED)
                    chatConfigurationBuilder.withEmailFieldStatus(PreChatFormFieldStatus.REQUIRED)
                    chatConfigurationBuilder.withPhoneFieldStatus(PreChatFormFieldStatus.REQUIRED)
                    chatConfigurationBuilder.withDepartmentFieldStatus(PreChatFormFieldStatus.REQUIRED)

                    if(withPreChatFormOptions.containsKey("withNameFieldStatus"))
                        chatConfigurationBuilder.withNameFieldStatus(getPreChatFieldStatus(withPreChatFormOptions["withNameFieldStatus"]))
                    if(withPreChatFormOptions.containsKey("withEmailFieldStatus"))
                        chatConfigurationBuilder.withNameFieldStatus(getPreChatFieldStatus(withPreChatFormOptions["withEmailFieldStatus"]))
                    if(withPreChatFormOptions.containsKey("withPhoneFieldStatus"))
                        chatConfigurationBuilder.withNameFieldStatus(getPreChatFieldStatus(withPreChatFormOptions["withPhoneFieldStatus"]))
                    if(withPreChatFormOptions.containsKey("withDepartmentFieldStatus"))
                        chatConfigurationBuilder.withNameFieldStatus(getPreChatFieldStatus(withPreChatFormOptions["withDepartmentFieldStatus"]))
                }

                MessagingActivity.builder()
                        .withBotLabelString(botLabel)
                        .withBotAvatarDrawable(botAvatar)
                        .withToolbarTitle(toolbarTitle)
                        .withEngines(ChatEngine.engine())
                        .show(activity, chatConfigurationBuilder.build())

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
