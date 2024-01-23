package irt.web.bean.email;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;

import irt.web.bean.ThreadRunner;
import irt.web.bean.jpa.WebContentRepository;
import okhttp3.Request;

@Service
public class GraphMailWorker implements MailWorker {
	private final Logger logger = LogManager.getLogger();

	@Autowired private WebContentRepository	 	webContentRepository;
	@Value("${app.graphUserScopes}")
	private String[] graphUserScopes;

    private GraphServiceClient<Request> graphClient;

    @PostConstruct
	public void GraphMailWorkerInit() {

    	ThreadRunner.runThread(
    			()->{
	
    			   	try {
//
//    			   		final List<WebContent> webContents = webContentRepository.findByPageName("email");
//						final IrtEMailData irtEMailData = new IrtEMailData(webContents);
//
//						final UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder()
//
//								.clientId(irtEMailData.getClientId())
//								.tenantId(irtEMailData.getTenantId())
//								.username(irtEMailData.getFrom())
//								.password(irtEMailData.getPassword())
//								.build();
//
//						if (null == graphUserScopes || null == credential)
//							throw new RuntimeException("Unexpected error");
//
//						final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(Arrays.asList(graphUserScopes), credential);

//						graphClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();

//						final Drive result = graphClient
//								  .me()
//								  .drive()
//								  .buildRequest()
//								  .get();
//								System.out.println("Found Drive " + result.id);
//
//								logger.error(result);

    			   	} catch (ClientException e) {
						logger.catching(e);
					}
    			});
    }

	private User getUser() {
		return graphClient.me()
        .buildRequest()
        .select("displayName,mail,userPrincipalName")
        .get();
	}

	@Override
//  Graph.sendMail("Testing Microsoft Graph", "Hello world!", email);
	public void sendEmail(String from, String subject, String body, BodyType bodyType, String... to) {
      logger.error("\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}",  from, subject, body, bodyType, to);

//		if (null == graphClient)
//			throw new RuntimeException("The graphClient object is not ceated");
//
//		final Drive result = graphClient
//      		  .me()
//      		  .drive()
//      		  .buildRequest()
//      		  .get();


//      // Create a new message
//        final Message message = new Message();
//        message.subject = subject;
//        message.body = new ItemBody();
//        message.body.content = body;
//        message.body.contentType = bodyType;
//
//        final List<Recipient> toRecipients = Arrays.stream(to)
//
//        		.map(
//        				address->{
//        					final Recipient toRecipient = new Recipient();
//        					toRecipient.emailAddress = new EmailAddress();
//        					toRecipient.emailAddress.address = address;
//        					return toRecipient;
//        				})
//        		.collect(Collectors.toList());
//
//        message.toRecipients = toRecipients;
//
//        // Send the message
//        graphClient.me()
//            .sendMail(UserSendMailParameterSet.newBuilder().withMessage(message).build())
//            .buildRequest()
//            .post();
    }
//
//    // <GetUserSnippet>
//    public User getUser() throws Exception {
//
//        return userClient.me()
//            .buildRequest()
//            .select("displayName,mail,userPrincipalName")
//            .get();
//    }
}
