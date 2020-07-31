package com.hgy.flutter.zd.flutter_zendes_plugin

import android.app.Activity
import android.content.Context
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
import zendesk.chat.Chat
import zendesk.chat.ChatConfiguration
import zendesk.chat.ChatEngine
import zendesk.chat.VisitorInfo
import zendesk.core.AnonymousIdentity
import zendesk.core.Zendesk
import zendesk.messaging.MessagingActivity
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
    private lateinit var context: Context
    private lateinit var activity: Activity

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_zendes_plugin")
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.applicationContext
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "flutter_zendes_plugin")
            channel.setMethodCallHandler(FlutterZendesPlugin())
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "init" -> {
                Zendesk.INSTANCE.init(this.context, "https://brplay.zendesk.com", "6783b5375f399d60da5242b77dc5fa5a888c51d39d841212", "mobile_sdk_client_a93b8a067e553b6f2f1f")
                Support.INSTANCE.init(Zendesk.INSTANCE)
                Chat.INSTANCE.init(this.context, "tFh19KFTd8BBqP3gihF65iF9ep7q4sLa")
                AnswerBot.INSTANCE.init(Zendesk.INSTANCE, Support.INSTANCE)
                Zendesk.INSTANCE.setIdentity(
                        AnonymousIdentity.Builder()
                                .withNameIdentifier("HGY")
                                .withEmailIdentifier("hobohoboom@gmail.com")
                                .build()
                )

                result.success("Init completed!")
            }
            "startChat" -> {

                val chatConfiguration = ChatConfiguration.builder()
                        .withPreChatFormEnabled(false)
                        .withAgentAvailabilityEnabled(false)
                        .build()
                val profileProvider = Chat.INSTANCE.providers()!!.profileProvider()
                val chatProvider = Chat.INSTANCE.providers()!!.chatProvider()

                val visitorInfo = VisitorInfo.builder()
                        .withName("Bob")
                        .withEmail("bob@example.com")
                        .withPhoneNumber("123456") // numeric string
                        .build()
                profileProvider.setVisitorInfo(visitorInfo, null)
                chatProvider.setDepartment("Department name", null)
                Zendesk.INSTANCE.setIdentity(AnonymousIdentity())
                MessagingActivity.builder()
                        .withEngines(SupportEngine.engine(), ChatEngine.engine(), AnswerBotEngine.engine())
                        .show(activity, chatConfiguration);
            }
            "helpCenter" -> {
                HelpCenterActivity.builder()
                        .show(activity)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
        TODO("Not yet implemented")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        TODO("Not yet implemented")
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        TODO("Not yet implemented")
    }
}
