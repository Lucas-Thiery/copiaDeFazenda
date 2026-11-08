package com.ufs.gestaofazenda.Config;

import com.ufs.gestaofazenda.Repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UsuarioRepository usuarioRepository;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return login -> {
            var usuario =
                    usuarioRepository
                            .findByLoginIgnoreCase(login)
                            .filter(u -> u.isAtivo() && u.getFuncionario().isAtivo())
                            .orElseThrow(
                                    () -> new UsernameNotFoundException("Credenciais inválidas."));
            return User.withUsername(usuario.getLogin())
                    .password(usuario.getSenhaHash())
                    .roles(usuario.getPerfil().name())
                    .build();
        };
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        a ->
                                a.requestMatchers(
                                                "/",
                                                "/fazendaweb/login.html",
                                                "/fazendaweb/setup.html",
                                                "/fazendaweb/style.css",
                                                "/fazendaweb/js/**",
                                                "/fazendaweb/imagens/**",
                                                "/css/**",
                                                "/js/**",
                                                "/imagens/**",
                                                "/actuator/health",
                                                "/api/auth/login",
                                                "/api/setup/inicial")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        e ->
                                e.authenticationEntryPoint(
                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .formLogin(
                        f ->
                                f.loginPage("/fazendaweb/login.html")
                                        .loginProcessingUrl("/api/auth/login")
                                        .usernameParameter("login")
                                        .passwordParameter("senha")
                                        .defaultSuccessUrl("/fazendaweb/principal.html", true)
                                        .failureUrl("/fazendaweb/login.html?erro")
                                        .permitAll())
                .logout(
                        l ->
                                l.logoutUrl("/api/auth/logout")
                                        .logoutSuccessUrl("/fazendaweb/login.html?logout")
                                        .invalidateHttpSession(true)
                                        .deleteCookies("JSESSIONID"))
                .csrf(
                        c ->
                                c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                        .csrfTokenRequestHandler(
                                                new CsrfTokenRequestAttributeHandler())
                                        .ignoringRequestMatchers(
                                                "/api/auth/login", "/api/setup/inicial"));
        http.addFilterBefore(
                new ContaAtivaFilter(usuarioRepository),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
