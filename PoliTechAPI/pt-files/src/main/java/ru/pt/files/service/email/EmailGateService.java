package ru.pt.files.service.email;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.email.EmailAttachment;
import ru.pt.api.dto.email.EmailMessage;
import ru.pt.api.service.file.FileService;
import ru.pt.api.service.process.FileProcessService;
import ru.pt.api.utils.JsonProjection;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class EmailGateService {

    private final Map<String, EmailClient> emailClients;
    private final AdminUserManagementService userManagementService;
    private final FileProcessService fileProcessService;

    public EmailGateService(Map<String, EmailClient> emailClients,
                            AdminUserManagementService userManagementService,
                            FileProcessService fileProcessService) {
        this.emailClients = emailClients;
        this.userManagementService = userManagementService;
        this.fileProcessService = fileProcessService;
    }

    public EmailClient resolveForCurrentUser(Long clientId) {
        Client client = userManagementService.getClientById(clientId);
        ClientConfiguration configuration = client.getClientConfiguration();
        if (configuration == null || !StringUtils.hasText(configuration.getEmailGate())) {
            throw new IllegalStateException("Email gate is not configured for client");
        }

        return resolveClient(configuration.getEmailGate());
    }

    public EmailMessage buildEmailMessage(PolicyData policyData) {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setSubject("Ваш договор страхования " + policyData.getPolicyNumber());
        JsonProjection jsonProjection = new JsonProjection(policyData.getPolicy());
        emailMessage.setTo(jsonProjection.getEmail());
        emailMessage.setBody("Ваш договор страхования во вложении");
        EmailAttachment emailAttachment = new EmailAttachment();
        emailAttachment.setFilename(policyData.getPolicyNumber() + ".pdf");
        emailAttachment.setContentType("application/pdf");
        emailAttachment.setContent(
                fileProcessService.generatePrintForm(policyData.getPolicyNumber(), "POLICY")
        );
        emailMessage.setAttachments(List.of(emailAttachment));
        return emailMessage;
    }

    private EmailClient resolveClient(String emailGate) {
        EmailClient client = emailClients.getOrDefault(
                emailGate.toLowerCase(Locale.ROOT),
                null);
        if (client == null) {
            throw new IllegalStateException("Unsupported email gate: " + emailGate);
        }
        return client;
    }

}
