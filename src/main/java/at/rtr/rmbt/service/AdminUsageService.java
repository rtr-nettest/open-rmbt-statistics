package at.rtr.rmbt.service;

import at.rtr.rmbt.response.adminUsage.AdminUsageJsonResponse;

import java.util.Set;

public interface AdminUsageService {

    AdminUsageJsonResponse getAdminUsageJson(Integer monthNumber, Integer yearNumber, Set<String> statistics);
}
