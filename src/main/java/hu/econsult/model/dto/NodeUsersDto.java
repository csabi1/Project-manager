package hu.econsult.model.dto;

import java.util.List;

import hu.econsult.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeUsersDto {

	private List<User> users;
}
