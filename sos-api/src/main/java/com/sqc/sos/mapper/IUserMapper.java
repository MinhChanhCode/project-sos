package com.sqc.sos.mapper;

import com.sqc.sos.dto.user.UserRequest;
import com.sqc.sos.dto.user.UserResponse;
import com.sqc.sos.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IUserMapper {
    User userRequestToUser(UserRequest userRequest);

    UserResponse userToUserResponse(User user);
}
