package br.com.incode.nexus_bagde.service;


import br.com.incode.nexus_bagde.dto.BadgeDTO;
import br.com.incode.nexus_bagde.entitys.Badge;
import br.com.incode.nexus_bagde.repository.BadgeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BadgeService {


    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${uploads.badges}")
    private String badgesDir;

    @Value("${uploads.issuers}")
    private String issuersDir;

    public List<BadgeDTO> getAllBadges() {
        return badgeRepository.findAll().stream()
                .map(badge -> modelMapper.map(badge, BadgeDTO.class))
                .collect(Collectors.toList());
    }

    public List<BadgeDTO> getActiveBadges() {
        return badgeRepository.findByIsActiveTrue().stream()
                .map(badge -> modelMapper.map(badge, BadgeDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<BadgeDTO> getBadgeById(Long id) {
        return badgeRepository.findById(id)
                .map(badge -> modelMapper.map(badge, BadgeDTO.class));
    }

    public List<BadgeDTO> getBadgesByCategory(String category) {
        return badgeRepository.findByCategoryAndIsActiveTrue(category).stream()
                .map(badge -> modelMapper.map(badge, BadgeDTO.class))
                .collect(Collectors.toList());
    }

    public BadgeDTO createBadge(BadgeDTO badgeDTO, MultipartFile imageFile, MultipartFile issuerImageFile) throws IOException {
        Badge badge = modelMapper.map(badgeDTO, Badge.class);

        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = saveImage(imageFile, badgesDir);
            badge.setImagePath(filename); // CORRIGIDO: Salva apenas o filename
            System.out.println("Badge criado - filename salvo no banco: " + filename);
        }

        if (issuerImageFile != null && !issuerImageFile.isEmpty()) {
            String filename = saveImage(issuerImageFile, issuersDir);
            badge.setIssuerImagePath(filename); // CORRIGIDO: Salva apenas o filename
            System.out.println("Emissor criado - filename salvo no banco: " + filename);
        }

        Badge savedBadge = badgeRepository.save(badge);
        return modelMapper.map(savedBadge, BadgeDTO.class);
    }

    public BadgeDTO updateBadge(Long id, BadgeDTO badgeDTO, MultipartFile imageFile, MultipartFile issuerImageFile) throws IOException {
        Badge existingBadge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge não encontrado"));

        existingBadge.setName(badgeDTO.getName());
        existingBadge.setDescription(badgeDTO.getDescription());
        existingBadge.setCategory(badgeDTO.getCategory());
        existingBadge.setIssuer(badgeDTO.getIssuer());
        existingBadge.setIsActive(badgeDTO.getIsActive());

        if (imageFile != null && !imageFile.isEmpty()) {
            // Deletar imagem antiga se existir
            if (existingBadge.getImagePath() != null) {
                deleteImageFile(existingBadge.getImagePath(), badgesDir);
            }
            String filename = saveImage(imageFile, badgesDir);
            existingBadge.setImagePath(filename); // CORRIGIDO: Salva apenas o filename
            System.out.println("Badge atualizado - filename salvo no banco: " + filename);
        }

        if (issuerImageFile != null && !issuerImageFile.isEmpty()) {
            // Deletar imagem antiga do emissor se existir
            if (existingBadge.getIssuerImagePath() != null) {
                deleteImageFile(existingBadge.getIssuerImagePath(), issuersDir);
            }
            String filename = saveImage(issuerImageFile, issuersDir);
            existingBadge.setIssuerImagePath(filename); // CORRIGIDO: Salva apenas o filename
            System.out.println("Emissor atualizado - filename salvo no banco: " + filename);
        }

        Badge updatedBadge = badgeRepository.save(existingBadge);
        return modelMapper.map(updatedBadge, BadgeDTO.class);
    }

    public void deleteBadge(Long id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge não encontrado"));

        if (badge.getImagePath() != null) {
            deleteImageFile(badge.getImagePath(), badgesDir);
        }

        if (badge.getIssuerImagePath() != null) {
            deleteImageFile(badge.getIssuerImagePath(), issuersDir);
        }

        badgeRepository.deleteById(id);
    }

    private String saveImage(MultipartFile file, String directory) throws IOException {
        // Criar diretório se não existir
        Path uploadPath = Paths.get(directory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Diretório criado: " + uploadPath.toAbsolutePath());
        }

        // Validar tipo de arquivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Arquivo deve ser uma imagem válida");
        }

        // Gerar nome único para o arquivo
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "image.png";
        }

        String extension = "";
        int lastDot = originalFilename.lastIndexOf(".");
        if (lastDot > 0) {
            extension = originalFilename.substring(lastDot);
        }

        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);

        // Salvar arquivo
        Files.copy(file.getInputStream(), filePath);

        System.out.println("Arquivo salvo fisicamente em: " + filePath.toAbsolutePath());
        System.out.println("Filename que será retornado: " + filename);

        // IMPORTANTE: Retornar APENAS o nome do arquivo
        return filename;
    }

    private void deleteImageFile(String filename, String directory) {
        try {
            Path filePath = Paths.get(directory + filename);
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                System.out.println("Arquivo deletado: " + filePath.toAbsolutePath());
            } else {
                System.out.println("Arquivo não encontrado para deletar: " + filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Erro ao deletar arquivo: " + e.getMessage());
        }
    }
}
