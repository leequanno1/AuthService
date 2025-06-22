package com.project.q_authent.services.authent_services;
import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.repositories.AccountRepository;
import com.project.q_authent.requests.auth.RegisterRequest;
import com.project.q_authent.responses.auth.TokenResponse;
import com.project.q_authent.utils.IDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

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
        accountRepository.save(account);

        return new TokenResponse(refreshToken,accessToken);
    }

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
                .active(false)
                .build();
        accountRepository.save(account);
        // TODO: send active account link that expire in 5 minutes
        return "OK";
    }


}
