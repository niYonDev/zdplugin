package com.hgy.flutter.zd.flutter_zendes_plugin

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import com.zopim.android.sdk.api.ZopimChat
import com.zopim.android.sdk.model.VisitorInfo
import com.zopim.android.sdk.prechat.PreChatForm
import com.zopim.android.sdk.prechat.ZopimChatActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import zendesk.core.AnonymousIdentity
import zendesk.core.Identity
import zendesk.core.Zendesk
import zendesk.support.Support
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
    private val SUBDOMAIN_URL = "https://brplay.zendesk.com"
    private val APPLICATION_ID = "6783b5375f399d60da5242b77dc5fa5a888c51d39d841212"
    private val OAUTH_CLIENT_ID = "mobile_sdk_client_a93b8a067e553b6f2f1f"
    private val ACCOUNT_KEY = "tFh19KFTd8BBqP3gihF65iF9ep7q4sLa"

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

                // Sample breadcrumb
                ZopimChat.trackEvent("Application Description ! Hello World!")

                /**
                 * Minimum chat configuration. Chat must be initialization before starting the chat.
                 */
                /**
                 * Minimum chat configuration. Chat must be initialization before starting the chat.
                 */
                ZopimChat.init(ACCOUNT_KEY)
                Support.INSTANCE.init(Zendesk.INSTANCE)
                Zendesk.INSTANCE.init(activity, SUBDOMAIN_URL, APPLICATION_ID, OAUTH_CLIENT_ID)

                val identity: Identity = AnonymousIdentity()
                Zendesk.INSTANCE.setIdentity(identity)

                Support.INSTANCE.init(Zendesk.INSTANCE)
                result.success("Init completed!")
            }
            "startChat" -> {

                buttonPreSetData()

            }
            "helpCenter" -> {
                HelpCenterActivity.builder()
                        .show(activity)
//                RequestListActivity.builder().show(context)
//                val intent = Intent(context, HelpCenterActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                context.startActivity(intent)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    /**
     * Pre-sets [com.zopim.android.sdk.model.VisitorInfo] data in the chat config and starts the new chat
     */
    private fun buttonPreSetData() {
        // build and set visitor info
        val visitorInfo = VisitorInfo.Builder()
                .phoneNumber("+8618521314531")
                .email("hobohoboom@gmail.com")
                .name("Grayson")
                .build()

        // visitor info can be set at any point when that information becomes available
        ZopimChat.setVisitorInfo(visitorInfo)

        // set pre chat fields as mandatory
        val preChatForm = PreChatForm.Builder()
                .name(PreChatForm.Field.REQUIRED_EDITABLE)
                .email(PreChatForm.Field.REQUIRED_EDITABLE)
                .phoneNumber(PreChatForm.Field.REQUIRED_EDITABLE)
                .department(PreChatForm.Field.REQUIRED_EDITABLE)
                .message(PreChatForm.Field.REQUIRED_EDITABLE)
                .build()

        // build chat config
        val config = ZopimChat.SessionConfig().preChatForm(preChatForm).department("Department My memory")

        // start chat activity with config
        ZopimChatActivity.startActivity(activity, config)

        // Sample breadcrumb
        ZopimChat.trackEvent("Started chat with pre-set visitor information,~~~~")
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
}
