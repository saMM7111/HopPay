package com.demo.hoppay.controller;

import com.demo.hoppay.model.TransactionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
	private final TransactionRepository transactionRepository;

	public DashboardController(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("transactions", transactionRepository.findAll());
		model.addAttribute("totalTransactions", transactionRepository.count());
		model.addAttribute("totalVolume", transactionRepository.totalVolume());
		return "dashboard";
	}
}
