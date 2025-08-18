package org.badmintonchain.service.impl;

import org.badmintonchain.exceptions.VerificationTokenException;
import org.badmintonchain.model.entity.UsersEntity;
import org.badmintonchain.model.entity.VerificationToken;
import org.badmintonchain.repository.UserRepository;
import org.badmintonchain.repository.VerificationTokenRepository;
import org.badmintonchain.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationTokenServiceImpl  implements VerificationTokenService {
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Override
    public String verifyAccount(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationTokenException("Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new VerificationTokenException("Verification token expired");
        }

        UsersEntity user = verificationToken.getUser();
        user.setActive(true);
        userRepository.save(user);

        return "Account verified successfully. You can now login.";
    }
}
