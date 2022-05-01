package hu.econsult.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import hu.econsult.service.impl.UserDetailsServiceImpl;

@Component
public class JwtFilter extends GenericFilterBean {
	
	public static final String AUTHORIZATION = "Authorization";
	
	private JwtProvider jwtProvider;
	private ValidatorService validatorService;
	private UserDetailsServiceImpl userDetailsServiceImpl;
	
	
	@Autowired
	public JwtFilter(JwtProvider jwtProvider, ValidatorService validatorService,
			UserDetailsServiceImpl userDetailsServiceImpl) {
		this.jwtProvider = jwtProvider;
		this.validatorService = validatorService;
		this.userDetailsServiceImpl = userDetailsServiceImpl;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String token = getTokenFromRequest(request);
		if (token != null) {
			String username = jwtProvider.getUsernameFromToken(token);
			UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
			if (userDetails != null && validatorService.isValidToken(token)) {
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
						userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}
		chain.doFilter(request, response);
	}
	
	private String getTokenFromRequest (ServletRequest request) {
		String authorization = ((HttpServletRequest) request).getHeader(AUTHORIZATION);
		if(null != authorization && authorization.startsWith("Bearer ") ) {
			return authorization.substring(7);
		}
		return null;	
	}
}
