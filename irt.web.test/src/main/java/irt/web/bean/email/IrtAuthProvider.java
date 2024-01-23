package irt.web.bean.email;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;

public class IrtAuthProvider implements IAuthenticationProvider {

	private CompletableFuture<String> accessTokenFuture;

	public IrtAuthProvider(String accessToken) {
        this.accessTokenFuture = new CompletableFuture<>();
        this.accessTokenFuture.complete(accessToken);
    }
	@Override
	public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
		 return this.accessTokenFuture;
	}    
}
