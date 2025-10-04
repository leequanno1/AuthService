package com.project.q_authent.services.authent_services;
import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.models.sqls.ValidationCode;
import com.project.q_authent.repositories.AccountRepository;
import com.project.q_authent.repositories.ValidationCodeRepository;
import com.project.q_authent.requests.auth.RegisterRequest;
import com.project.q_authent.responses.auth.TokenResponse;
import com.project.q_authent.utils.IDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * SecurityService
 * Last update 2025/07/26
 * @since 1.00
 * @author leequanno1
 */
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidationCodeRepository validationCodeRepository;

    /**
     * Handle login for service user
     * @param username {@link String}
     * @param password {@link String}
     * @return TokenResponse if success, otherwise throw BadException
     * @since 1.00
     */
    public TokenResponse login(String username, String password) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new BadException(ErrorCode.WRONG_PASSWORD);
        }

        if (!account.getActive()) {
            throw new BadException(ErrorCode.ACCOUNT_UNACTIVATED);
        }

        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken(account);

        return new TokenResponse(refreshToken,accessToken);
    }

    /**
     * Handle refresh access token and refresh token
     * @param refreshToken {@link String}
     * @return TokenResponse if success, otherwise throw BadException
     * @since 1.00
     */
    public TokenResponse refreshToken(String refreshToken) {
        String accountId = jwtService.extractAccountId(refreshToken, true);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        if (!jwtService.isTokenValid(refreshToken, account, true)) {
            throw new BadException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtService.generateAccessToken(account);
        String newRefreshToken = jwtService.generateRefreshToken(account);
        accountRepository.save(account);

        return new TokenResponse(newRefreshToken, newAccessToken);
    }

    /**
     * Handle registration new service user
     * @param request {@link RegisterRequest}
     * @return String OK if success, otherwise throw DadException
     * @since 1.00
     */
    public String register(RegisterRequest request) {
        // check existed user
        if(accountRepository.findByUsername(request.getUsername()).isPresent())
            throw new BadException(ErrorCode.USER_EXISTED);
        // check exited email
        if(accountRepository.findByEmail(request.getEmail()).isPresent())
            throw new BadException(ErrorCode.EMAIL_USED);
        // create user
        Account account = Account.builder()
                .accountId(IDUtil.getID(TableIdHeader.ACCOUNT_HEADER))
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .displayName(request.getDisplayName())
                .active(true)
                .build();
        accountRepository.save(account);
        // TODO: send active account link that expire in 5 minutes
        return "OK";
    }

    /**
     * Active sub user account by checking validation code, use after send code success
     * @param accountId {@link String}
     * @param code {@link String}
     * @return new Token pair {@link TokenResponse}
     */
    public TokenResponse activeSubUser(String accountId, String code) {

        // check valid code
        ValidationCode validationCode = validationCodeRepository
                .findByTargetAccount_AccountIdOrderByExpireTimeDesc(accountId)
                .orElseThrow(() -> new BadException(ErrorCode.VALIDATION_CODE_NOT_FOUND));
        if (validationCode.getExpireTime().before(Timestamp.valueOf(LocalDateTime.now()))) {
            throw new BadException(ErrorCode.VALIDATION_CODE_EXPIRED);
        }

        if (!validationCode.getCodeValue().equals(Integer.parseInt(code))) {
            throw new BadException(ErrorCode.INVALID_TOKEN);
        }
        // deleteCode
        validationCodeRepository.delete(validationCode);
        // set active = true
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        account.setActive(true);
        accountRepository.save(account);
        // sign new token
        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken(account);

        return new TokenResponse(refreshToken,accessToken);
    }
}
