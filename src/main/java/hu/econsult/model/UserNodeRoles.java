package hu.econsult.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import lombok.experimental.UtilityClass;

@UtilityClass
public class UserNodeRoles {
	public static final String READ_ROLE = "ROLE_READ";
	
	public static final String MODIFY_ROLE = "ROLE_MODIFY";
	
	public static final String DELETE_ROLE = "ROLE_DELETE";
	
	public static final String ADMIN_ROLE = "ROLE_ADMIN";
	
	public static final List<String> ROLE_HIERARCHY = new ArrayList<>(
		    Arrays.asList(READ_ROLE, MODIFY_ROLE, DELETE_ROLE, ADMIN_ROLE)
		);
}
