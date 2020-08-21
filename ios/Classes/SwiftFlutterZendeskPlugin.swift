import Flutter
import UIKit
import SupportSDK
import ZendeskCoreSDK
import ChatSDK
import ChatProvidersSDK
import ZendeskCoreSDK
import MessagingSDK

public class SwiftFlutterZendeskPlugin: NSObject, FlutterPlugin {
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_zendes_plugin", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterZendeskPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    private func getPreChatFieldStatus(status: String) -> FormFieldStatus {
        switch status.uppercased() {
        case "REQUIRED":
            return FormFieldStatus.required
        case "HIDDEN":
            return FormFieldStatus.hidden
        case "OPTIONAL":
            return FormFieldStatus.optional
        default:
            return FormFieldStatus.required
        }
        
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
            case "init":
                // Set logger on debug
                Logger.isEnabled = true
                Logger.defaultLevel = .verbose

                // Get variables from flutter
                guard let dic = call.arguments as? Dictionary<String, Any> else { return }
                let accountKey = dic["accountKey"] as? String ?? ""
                let applicationId = dic["applicationId"] as? String ?? ""
                let clientId = dic["clientId"] as? String ?? ""
                let domainUrl = dic["domainUrl"] as? String ?? ""

                if(accountKey.isEmpty) {
                    result("AccountKey is null")
                    break
                }

                // 1.Init Zendesk SDK instance
                Zendesk.initialize(appId: applicationId,
                                   clientId: clientId,
                                   zendeskUrl: domainUrl)

                // 2.Init Support SDK instance
                Support.initialize(withZendesk: Zendesk.instance)

                // 3.Setting Anonymous identity for Zendesk SDK
                Zendesk.instance?.setIdentity(Identity.createAnonymous())

                // 4.Init ChatV2 SDK Instance
                Chat.initialize(accountKey: accountKey,appId:applicationId)

                result("iOS init completed" )
            case "startChatV2":
                
                
                guard let dic = call.arguments as? Dictionary<String, Any> else { return }
                
                
                // Get Visitor variables
                let visitorPhone = dic["visitorPhone"] as? String ?? ""
                let visitorEmail = dic["visitorEmail"] as? String ?? ""
                let visitorName = dic["visitorName"] as? String ?? ""
                let visitorTags = dic["visitorTags"] as? Array<String> ?? [String]()
                // Notes not available in iOS
                // let visitorNotes = dic["visitorNotes"] as? String ?? ""
                
                // Variables Chat features
                let departmentName = dic["departmentName"] as? String ?? "Department name"
                let botLabel = dic["botLabel"] as? String ?? "Bot"
                let botAvatar = dic["botAvatar"] as? String
                let toolbarTitle = dic["toolbarTitle"] as? String ?? "Chat"
                let withAgentAvailabilityEnabled = dic["withAgentAvailabilityEnabled"] as? Bool ?? true
                let withChatTranscriptsEnabled = dic["withChatTranscriptsEnabled"] as? Bool ?? false
                let withPreChatFormEnabled = dic["withPreChatFormEnabled"] as? Bool ?? true
                let withPreChatFormOptions = dic["withPreChatFormOptions"] as? Dictionary<String, String> ?? [String: String]()
                let withOfflineFormsEnabled = dic["withOfflineFormsEnabled"] as? Bool ?? true
                let withChatMenuActions = dic["withChatMenuActions"] as? String
            
                // Init configurations
                let chatAPIConfiguration = ChatAPIConfiguration()
                let messagingConfiguration = MessagingConfiguration()
                let chatConfiguration = ChatConfiguration()
            
                // Visitor Chat Builders
                let visitorInfo = VisitorInfo(name: visitorName, email: visitorEmail, phoneNumber: visitorPhone)
                
                // ChatAPIConfiguration values
                chatAPIConfiguration.visitorInfo = visitorInfo
                if !visitorTags.isEmpty {
                    chatAPIConfiguration.tags = visitorTags
                }
                chatAPIConfiguration.department = departmentName
                
                // ChatConfiguration values
                chatConfiguration.isChatTranscriptPromptEnabled = withChatTranscriptsEnabled
                chatConfiguration.isPreChatFormEnabled = withPreChatFormEnabled
                chatConfiguration.isOfflineFormEnabled = withOfflineFormsEnabled
                chatConfiguration.isAgentAvailabilityEnabled = withAgentAvailabilityEnabled
                
                /**
                 *  Set all fields REQUIRED, if detects the parameter used in the HashMap withPreChatFormOptions,
                 *  get its current String value and convert it to PreChatFormFieldStatus,
                 *  then set it in ChatConfigurationBuilder
                 */
                if withPreChatFormEnabled {
                    var nameStatus = FormFieldStatus.required
                    var emailStatus = FormFieldStatus.required
                    var phoneStatus = FormFieldStatus.required
                    var departmentStatus = FormFieldStatus.required
                    
                    if let name = withPreChatFormOptions["withNameFieldStatus"] {
                        nameStatus = getPreChatFieldStatus(status: name)
                    }
                    if let email = withPreChatFormOptions["withEmailFieldStatus"] {
                        emailStatus = getPreChatFieldStatus(status: email)
                    }
                    if let phone = withPreChatFormOptions["withPhoneFieldStatus"] {
                        phoneStatus = getPreChatFieldStatus(status: phone)
                    }
                    if let department = withPreChatFormOptions["withDepartmentFieldStatus"] {
                        departmentStatus = getPreChatFieldStatus(status: department)
                    }
                    
                    let formConfiguration = ChatFormConfiguration(
                        name: nameStatus,
                        email: emailStatus,
                        phoneNumber: phoneStatus,
                        department: departmentStatus
                    )
                    
                    chatConfiguration.preChatFormConfiguration = formConfiguration
                }
                
                // MessagingConfiguration values
                messagingConfiguration.name = botLabel
            
                
                
        
                do {
                    let chatEngine = try ChatEngine.engine()
                    let viewController = try Messaging.instance.buildUI(engines: [chatEngine], configs: [chatConfiguration, messagingConfiguration])
                    
                    if let navigationController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController {
                        navigationController.pushViewController(viewController, animated: true)
                        
                        Chat.instance?.configuration = chatAPIConfiguration
                    }
                } catch let error {
                    
                }
                
    
                
            case "helpCenter":
                // Get variables from flutter
                guard let dic = call.arguments as? Dictionary<String, Any> else { return }
                let categoryShowContactOptions = dic["categoryShowContactOptions"] as? Bool ?? true
                let categoryShowContactOptionsOnEmptySearch = dic["categoryShowContactOptionsOnEmptySearch"] as? Bool ?? true
                let articleShowContactOptions = dic["articleShowContactOptions"] as? Bool ?? true

                let currentVC = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController

                // Category configuration
                let hcConfig = HelpCenterUiConfiguration()
                hcConfig.showContactOptions = categoryShowContactOptions
                hcConfig.showContactOptionsOnEmptySearch = categoryShowContactOptionsOnEmptySearch

                // Articles configuration
                let articleUiConfig = ArticleUiConfiguration()
                articleUiConfig.showContactOptions = articleShowContactOptions

                // Start HelpCenter
                let helpCenter = HelpCenterUi.buildHelpCenterOverviewUi(withConfigs: [hcConfig])
                currentVC?.pushViewController(helpCenter, animated: true)

                result("iOS helpCenter UI:" + helpCenter.description + "   ")
            case "requestView":
                let rootViewController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
                let viewController = RequestUi.buildRequestUi(with: [])
                rootViewController?.pushViewController(viewController, animated: true)
            case "requestListView":
                let rootViewController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
                let viewController = RequestUi.buildRequestList(with: [])
                rootViewController?.pushViewController(viewController, animated: true)
            case "changeNavStatus":
                let rootViewController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
                guard let dic = call.arguments as? Dictionary<String, Any> else { return }

                let isShow = dic["isShow"] as? Bool ?? false
                rootViewController?.setNavigationBarHidden(!isShow, animated: false)
                result("rootViewController?.isNavigationBarHidden = isShow >>>>>")
            case "getPlatformVersion":
                result("iOS " + UIDevice.current.systemVersion)

            default:
                result("method not implemented")
        }
    }
    
    func startChatV2(botLabel:String,phone:String,name:String) throws {
        
        //Chat.profileProvider?.addTags(["teste：name："+name+" phone："+phone])
        
        let chatFormConfiguration = ChatSDK.ChatFormConfiguration.init(name: .required, email: .hidden, phoneNumber: .hidden, department: .hidden)
        
        let chatConfiguration = ChatConfiguration()
        //If true, visitors will be prompted at the end of their chat asking them whether they would like a transcript sent by email.
        chatConfiguration.isChatTranscriptPromptEnabled = true
        //If true, visitors are prompted for information in a conversational manner prior to starting the chat. Defaults to true.
        chatConfiguration.isPreChatFormEnabled = true
        //If this flag is enabled (as well as isAgentAvailabilityEnabled) then visitors will be presented with a form allowing them to leave a message if no agents are available. This will create a support ticket. Defaults to true.
        chatConfiguration.isOfflineFormEnabled = true
        //If true, and no agents are available to serve the visitor, they will be presented with a message letting them know that no agents are available. If it's disabled, visitors will remain in a queue waiting for an agent. Defaults to true.
        chatConfiguration.isAgentAvailabilityEnabled = true
        //This property allows you to configure the requirements of each of the pre-chat form fields.
        chatConfiguration.preChatFormConfiguration = chatFormConfiguration
        
        // Name for Bot messages
        let messagingConfiguration = MessagingConfiguration()
        messagingConfiguration.name = botLabel
        
        let chatEngine = try ChatEngine.engine()
        
        let viewController = try Messaging.instance.buildUI(engines: [chatEngine], configs: [chatConfiguration,messagingConfiguration])
        
        
        if let navigationController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController {
            navigationController.pushViewController(viewController, animated: true)
            let chatAPIConfiguration = ChatAPIConfiguration()
            chatAPIConfiguration.visitorInfo = VisitorInfo(name: name, email: "", phoneNumber: phone)
            Chat.instance?.configuration = chatAPIConfiguration
        }
        
        
    }
    
}
