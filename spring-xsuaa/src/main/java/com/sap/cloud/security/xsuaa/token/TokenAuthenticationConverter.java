package com.sap.cloud.security.xsuaa.token;

import com.sap.cloud.security.xsuaa.extractor.AuthoritiesExtractor;
import com.sap.cloud.security.xsuaa.extractor.DefaultAuthoritiesExtractor;
import com.sap.cloud.security.xsuaa.extractor.LocalAuthoritiesExtractor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import org.springframework.util.Assert;

/**
 * An authentication converter that removes the ugly application id prefix (e.g.
 * my-application-demo!t1229) from the scopes in the JWT.
 *
 */
public class TokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private AuthoritiesExtractor authoritiesExtractor;
	private String appId;

	/**
	 * Creates a new converter with the given {@link AuthoritiesExtractor}.
	 * 
	 * @param authoritiesExtractor
	 *            - the extractor used to turn Jwt scopes into Spring Security
	 *            authorities.
	 */
	public TokenAuthenticationConverter(AuthoritiesExtractor authoritiesExtractor) {
		this.authoritiesExtractor = authoritiesExtractor;
	}

	/**
	 * Creates a new converter with a new {@link DefaultAuthoritiesExtractor}
	 * instance as default authorities extractor.
	 * 
	 * @param appId
	 *            e.g. myXsAppname!t123
	 */
	public TokenAuthenticationConverter(String appId) {
		authoritiesExtractor = new DefaultAuthoritiesExtractor();
		this.appId = appId;
	}

	/**
	 * Creates a new converter with a new {@link DefaultAuthoritiesExtractor}
	 * instance as default authorities extractor.
	 *
	 * @param xsuaaServiceConfiguration
	 *            the xsuaa configuration
	 */
	public TokenAuthenticationConverter(XsuaaServiceConfiguration xsuaaServiceConfiguration) {
		this(xsuaaServiceConfiguration.getAppId());
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		return new AuthenticationToken(jwt, authoritiesExtractor.getAuthorities(new XsuaaToken(jwt)));
	}

	/**
	 * This method allows to overwrite the default behavior of the
	 * {@link Token#getAuthorities()} implementation. Creates a new converter with a
	 * new {@link LocalAuthoritiesExtractor}
	 *
	 * @param extractLocalScopesOnly
	 *            true when {@link Token#getAuthorities()} should only extract local
	 *            scopes. Local scopes means that non-application specific scopes
	 *            are filtered out and scopes are returned without appId prefix,
	 *            e.g. "Display".
	 */
	public void setLocalScopeAsAuthorities(boolean extractLocalScopesOnly) {
		if (extractLocalScopesOnly) {
			Assert.state(appId != null,
					"For local Scope extraction 'appId' must be provided to `TokenAuthenticationConverter`");
			authoritiesExtractor = new LocalAuthoritiesExtractor(appId);
		} else {
			authoritiesExtractor = new DefaultAuthoritiesExtractor();
		}
	}

}