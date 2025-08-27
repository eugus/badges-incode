package br.com.incode.nexus_bagde.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/uploads")

public class FileController {


    private final String uploadDir = "uploads/";

    @GetMapping("/badges/{filename:.+}")
    public ResponseEntity<Resource> getBadgeImage(@PathVariable String filename) {
        System.out.println("🔍 FileController - Requisição para badge: " + filename);

        try {
            Path filePath = Paths.get(uploadDir + "badges/" + filename);
            System.out.println("📁 Caminho completo do arquivo: " + filePath.toAbsolutePath());
            System.out.println("📂 Diretório existe: " + Files.exists(filePath.getParent()));
            System.out.println("📄 Arquivo existe: " + Files.exists(filePath));

            if (Files.exists(filePath)) {
                System.out.println("✅ Arquivo encontrado, tamanho: " + Files.size(filePath) + " bytes");
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                System.out.println("❌ Arquivo não existe ou não é legível");
                System.out.println("   - Existe: " + resource.exists());
                System.out.println("   - Legível: " + (resource.exists() ? resource.isReadable() : "N/A"));

                // Listar arquivos no diretório para debug
                Path badgesDir = Paths.get(uploadDir + "badges/");
                if (Files.exists(badgesDir)) {
                    System.out.println("📋 Arquivos no diretório badges:");
                    Files.list(badgesDir).forEach(file ->
                            System.out.println("   - " + file.getFileName())
                    );
                } else {
                    System.out.println("❌ Diretório badges não existe: " + badgesDir.toAbsolutePath());
                }

                return ResponseEntity.notFound().build();
            }

            // Determinar o tipo de conteúdo
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            System.out.println("📋 Content-Type detectado: " + contentType);

            System.out.println("✅ Retornando arquivo com sucesso");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.out.println("💥 Erro no FileController: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/issuers/{filename:.+}")
    public ResponseEntity<Resource> getIssuerImage(@PathVariable String filename) {
        System.out.println("🏢 ===== REQUISIÇÃO EMISSOR =====");
        System.out.println("🏢 FileController - Requisição para emissor: " + filename);
        System.out.println("🏢 Timestamp: " + java.time.LocalDateTime.now());

        try {
            Path filePath = Paths.get(uploadDir + "issuers/" + filename);
            System.out.println("🏢 Caminho completo: " + filePath.toAbsolutePath());
            System.out.println("🏢 Diretório pai existe: " + Files.exists(filePath.getParent()));
            System.out.println("🏢 Arquivo existe: " + Files.exists(filePath));

            // Criar diretório se não existir
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
                System.out.println("🏢 Diretório issuers criado: " + filePath.getParent().toAbsolutePath());
            }

            if (Files.exists(filePath)) {
                System.out.println("🏢 ✅ Arquivo encontrado!");
                System.out.println("🏢 Tamanho: " + Files.size(filePath) + " bytes");
                System.out.println("🏢 Legível: " + Files.isReadable(filePath));
            } else {
                System.out.println("🏢 ❌ Arquivo NÃO encontrado!");
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                System.out.println("🏢 ❌ Resource não existe ou não é legível");
                System.out.println("🏢    - Resource existe: " + resource.exists());
                System.out.println("🏢    - Resource legível: " + (resource.exists() ? resource.isReadable() : "N/A"));

                // Listar TODOS os arquivos no diretório
                Path issuersDir = Paths.get(uploadDir + "issuers/");
                if (Files.exists(issuersDir)) {
                    System.out.println("🏢 📋 LISTANDO arquivos no diretório issuers:");
                    Files.list(issuersDir).forEach(file -> {
                        try {
                            System.out.println("🏢    - " + file.getFileName() + " (tamanho: " + Files.size(file) + " bytes)");
                        } catch (IOException e) {
                            System.out.println("🏢    - " + file.getFileName() + " (erro ao ler tamanho)");
                        }
                    });
                } else {
                    System.out.println("🏢 ❌ Diretório issuers não existe: " + issuersDir.toAbsolutePath());
                }

                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            System.out.println("🏢 ✅ SUCESSO - Retornando arquivo emissor!");
            System.out.println("🏢 Content-Type: " + contentType);
            System.out.println("🏢 ===== FIM REQUISIÇÃO EMISSOR =====");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.out.println("🏢 💥 ERRO CRÍTICO no FileController (emissor): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String folder, @PathVariable String filename) {
        System.out.println("🔍 FileController - Requisição genérica: " + folder + "/" + filename);

        try {
            Path filePath = Paths.get(uploadDir + folder + "/" + filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("💥 Erro no FileController (genérico): " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
