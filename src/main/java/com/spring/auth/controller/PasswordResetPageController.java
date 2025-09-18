package com.spring.auth.controller;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.auth.service.PasswordResetService;

@Controller
@RequiredArgsConstructor
public class PasswordResetPageController {

  private final PasswordResetService passwordResetService;

  // 링크 클릭 → 폼 렌더
  @GetMapping("/reset-password")
  public String view(@RequestParam("token") String token, Model model) {
    if (!passwordResetService.validateToken(token)) {
      return "client/reset-invalid"; // 만료/사용/위조
    }
    model.addAttribute("token", token);
    return "client/reset-password";  // 네가 만든 템플릿
  }

  // 새 비밀번호 제출
  @PostMapping("/reset-password")
  public String submit(@RequestParam("token") String token,
                       @RequestParam("newPwd") String newPwd,
                       @RequestParam("newPwdConfirm") String newPwdConfirm,
                       RedirectAttributes ra) {
    if (!Objects.equals(newPwd, newPwdConfirm)) {
      ra.addFlashAttribute("err", "비밀번호가 일치하지 않습니다.");
      ra.addAttribute("token", token);
      return "redirect:/reset-password";
    }
    try {
      passwordResetService.resetPassword(token, newPwd);
      ra.addFlashAttribute("msg", "비밀번호가 변경되었습니다. 로그인해 주세요.");
      return "redirect:/loginPage";
    } catch (Exception e) {
      ra.addFlashAttribute("err", "링크가 만료되었거나 유효하지 않습니다.");
      return "redirect:/loginPage";
    }
  }
}
