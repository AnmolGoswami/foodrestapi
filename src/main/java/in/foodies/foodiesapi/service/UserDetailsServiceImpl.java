package in.foodies.foodiesapi.service;

import in.foodies.foodiesapi.repositary.UserRepositary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepositary userRepositary;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepositary.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("User Not Exist"));
    }
}
