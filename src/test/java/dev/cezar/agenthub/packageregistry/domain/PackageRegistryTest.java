package dev.cezar.agenthub.packageregistry.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PackageRegistryTest {

    @Test
    void shouldBuildPackageRegistryWithAllFields() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        PackageRegistry pkg = PackageRegistry.builder()
                .id(id)
                .name("My Agent")
                .slug("my-agent")
                .namespace("dev.cezar")
                .packageType("AGENT")
                .description("A powerful agent")
                .latestVersion("1.2.0")
                .visibility("PUBLIC")
                .provider("cezar-dev")
                .repositoryUrl("https://github.com/example/my-agent")
                .documentationUrl("https://docs.example.com/my-agent")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(pkg.getId()).isEqualTo(id);
        assertThat(pkg.getName()).isEqualTo("My Agent");
        assertThat(pkg.getSlug()).isEqualTo("my-agent");
        assertThat(pkg.getNamespace()).isEqualTo("dev.cezar");
        assertThat(pkg.getPackageType()).isEqualTo("AGENT");
        assertThat(pkg.getDescription()).isEqualTo("A powerful agent");
        assertThat(pkg.getLatestVersion()).isEqualTo("1.2.0");
        assertThat(pkg.getVisibility()).isEqualTo("PUBLIC");
        assertThat(pkg.getProvider()).isEqualTo("cezar-dev");
        assertThat(pkg.getCreatedAt()).isEqualTo(now);
        assertThat(pkg.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSupportMutableFieldUpdates() {
        PackageRegistry pkg = PackageRegistry.builder()
                .id(UUID.randomUUID())
                .name("Old Name")
                .slug("old-slug")
                .packageType("SKILL")
                .visibility("PRIVATE")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        pkg.setName("New Name");
        pkg.setLatestVersion("2.0.0");
        pkg.setVisibility("PUBLIC");

        assertThat(pkg.getName()).isEqualTo("New Name");
        assertThat(pkg.getLatestVersion()).isEqualTo("2.0.0");
        assertThat(pkg.getVisibility()).isEqualTo("PUBLIC");
    }

    @Test
    void shouldCreatePackageWithMinimalFields() {
        PackageRegistry pkg = PackageRegistry.builder()
                .name("Minimal Package")
                .slug("minimal-pkg")
                .packageType("TOOL")
                .build();

        assertThat(pkg.getName()).isEqualTo("Minimal Package");
        assertThat(pkg.getSlug()).isEqualTo("minimal-pkg");
        assertThat(pkg.getPackageType()).isEqualTo("TOOL");
        assertThat(pkg.getId()).isNull();
        assertThat(pkg.getDescription()).isNull();
        assertThat(pkg.getLatestVersion()).isNull();
    }

    @Test
    void shouldSupportEqualityBasedOnAllFields() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        PackageRegistry pkg1 = PackageRegistry.builder()
                .id(id).name("Agent A").slug("agent-a")
                .packageType("AGENT").visibility("PUBLIC")
                .createdAt(now).updatedAt(now)
                .build();

        PackageRegistry pkg2 = PackageRegistry.builder()
                .id(id).name("Agent A").slug("agent-a")
                .packageType("AGENT").visibility("PUBLIC")
                .createdAt(now).updatedAt(now)
                .build();

        assertThat(pkg1).isEqualTo(pkg2);
        assertThat(pkg1.hashCode()).isEqualTo(pkg2.hashCode());
    }

    @Test
    void shouldHaveStringRepresentation() {
        PackageRegistry pkg = PackageRegistry.builder()
                .id(UUID.randomUUID())
                .name("Test Package")
                .slug("test-pkg")
                .packageType("AGENT")
                .build();

        String toString = pkg.toString();
        assertThat(toString).contains("test-pkg");
        assertThat(toString).contains("Test Package");
    }
}
