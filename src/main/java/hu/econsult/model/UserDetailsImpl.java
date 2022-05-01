package hu.econsult.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import hu.econsult.model.entity.ProjectRole;
import hu.econsult.model.entity.User;

public class UserDetailsImpl implements UserDetails {

	private static final long serialVersionUID = 4989499927385994108L;

	private User user;
	
	public UserDetailsImpl(User user){
		this.user = user;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new HashSet<>();
		Set<ProjectRole> roles = user.getProjectRoles();
		for(ProjectRole role : roles) {
			authorities.add(new SimpleGrantedAuthority(role.getProjectRole()));
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		return this.user.getPassword();
	}

	@Override
	public String getUsername() {
		return this.user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}
}
