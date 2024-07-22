package pl.rg.impl;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserImpl implements User {

    private Integer id;

    private String userName;

    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private String role;

    public UserImpl(String userName, String password, String firstName, String lastName, String email, String role) {
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }
}
