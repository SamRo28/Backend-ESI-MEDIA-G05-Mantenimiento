package com.EsiMediaG03.EsiMediaContenidosG03;

import com.EsiMediaG03.dao.ContenidoDAO;
import com.EsiMediaG03.exceptions.ContenidoAddException;
import com.EsiMediaG03.model.Contenido;
import com.EsiMediaG03.services.ContenidoService;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContenidoServiceTest {

    @Mock
    ContenidoDAO contenidoDAO;

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    ContenidoService service;

    private static Contenido baseAudio() {
        Contenido c = new Contenido();
        c.setTipo(Contenido.Tipo.AUDIO);
        c.setTitulo("Mi audio");
        c.setTags(List.of("tag1"));
        c.setDuracionMinutos(3);
        c.setFicheroAudio("/path/audio.mp3");
        return c;
    }

    private static Contenido baseVideo(String resolucion) {
        Contenido c = new Contenido();
        c.setTipo(Contenido.Tipo.VIDEO);
        c.setTitulo("Mi vídeo");
        c.setTags(List.of("tag1"));
        c.setDuracionMinutos(5);
        c.setUrlVideo("https://video.example/video-1");
        c.setResolucion(resolucion);
        return c;
    }

    // ====================== TESTS EXISTENTES ======================

    @Test
    void anadirContenido_audio_ok() throws Throwable {
        Contenido c = baseAudio();
        when(contenidoDAO.save(any(Contenido.class))).thenAnswer(inv -> inv.getArgument(0));

        Contenido res = service.anadirContenido(c);

        assertSame(c, res);
        verify(contenidoDAO).save(c);
    }

    @ParameterizedTest
    @ValueSource(strings = {"720p", "1080p", "4k", "4K"})
    void anadirContenido_video_ok_resoluciones(String reso) throws Throwable {
        Contenido c = baseVideo(reso);
        when(contenidoDAO.save(any(Contenido.class))).thenAnswer(inv -> inv.getArgument(0));

        Contenido out = service.anadirContenido(c);

        assertSame(c, out);
        verify(contenidoDAO).save(c);
    }

    @Test
    void tipo_null_lanza() {
        Contenido c = baseAudio();
        c.setTipo(null);

        ContenidoAddException ex = assertThrows(ContenidoAddException.class, () -> service.anadirContenido(c));
        assertTrue(ex.getMessage().toLowerCase().contains("tipo"));
        verify(contenidoDAO, never()).save(any());
    }

    // ... otros tests de validación (igual que antes) ...

    // ====================== TESTS ESTADISTICAS ======================

    @Test
    void estadisticasGlobales_devuelve_tres_listas() {
        when(mongoTemplate.find(any(Query.class), eq(Contenido.class)))
                .thenReturn(List.of(new Contenido()));

        Map<String, Object> res = service.estadisticasGlobales();

        assertTrue(res.containsKey("topReproducciones"));
        assertTrue(res.containsKey("topValoraciones"));
        assertTrue(res.containsKey("topCategorias"));
    }

    @Test
    void top5PorReproducciones_ok() {
        Contenido c1 = new Contenido();
        c1.setId("id1");
        c1.setTitulo("Uno");
        c1.setTipo(Contenido.Tipo.AUDIO);
        c1.setNumReproducciones(10L); // Asegúrate de usar Long

        Contenido c2 = new Contenido();
        c2.setId("id2");
        c2.setTitulo("Dos");
        c2.setTipo(Contenido.Tipo.VIDEO);
        c2.setNumReproducciones(5L);

        when(mongoTemplate.find(any(Query.class), eq(Contenido.class)))
                .thenReturn(List.of(c1, c2));

        Map<String, Object> res = service.estadisticasGlobales();
        List<Map<String, Object>> top = (List<Map<String, Object>>) res.get("topReproducciones");

        assertEquals("id1", top.get(0).get("id"));
        assertEquals(10L, top.get(0).get("reproducciones"));
    }


    @Test
    void top5PorValoraciones_filtra_ratingCount() {
        Contenido c1 = new Contenido();
        c1.setId("v1");
        c1.setTitulo("V1");
        c1.setTipo(Contenido.Tipo.VIDEO);
        c1.setRatingCount(2);
        c1.setRatingAvg(4.0);

        when(mongoTemplate.find(any(Query.class), eq(Contenido.class))).thenReturn(List.of(c1));

        Map<String, Object> res = service.estadisticasGlobales();
        List<Map<String, Object>> top = (List<Map<String, Object>>) res.get("topValoraciones");

        assertEquals(1, top.size());
        assertEquals("v1", top.get(0).get("id"));
        assertEquals(4.0, top.get(0).get("avg"));
    }

@Test
void top5CategoriasMasVistas_ok() {
    Contenido c1 = new Contenido();
    c1.setUserEmail("a@a.com");
    c1.setNumReproducciones(10L);

    Contenido c2 = new Contenido();
    c2.setUserEmail("b@b.com");
    c2.setNumReproducciones(20L);

    Document userA = new Document("email", "a@a.com").append("especialidad", "Informatica");
    Document userB = new Document("email", "b@b.com").append("especialidad", "Medicina");

    when(mongoTemplate.find(any(Query.class), eq(Contenido.class)))
            .thenReturn(List.of(c1, c2));
    when(mongoTemplate.find(any(Query.class), eq(Document.class), anyString()))
            .thenReturn(List.of(userA, userB));

    Map<String, Object> res = service.estadisticasGlobales();
    List<Map<String, Object>> top = (List<Map<String, Object>>) res.get("topCategorias");

    assertEquals(2, top.size());
    assertEquals("Medicina", top.get(0).get("especialidad"));
    assertEquals(20L, top.get(0).get("reproducciones"));    
}

    @Test
    void storeAudioFile_savesFileAndUpdatesContenido() throws Exception {
        // Preparar contenido existente
        Contenido c = baseAudio();
        c.setId("cid1");

        when(contenidoDAO.findById("cid1")).thenReturn(Optional.of(c));
        when(contenidoDAO.save(any(Contenido.class))).thenAnswer(inv -> inv.getArgument(0));

        // Crear archivo mp3 falso (con header ID3)
        byte[] fakeMp3 = new byte[] {0x49, 0x44, 0x33, 0x00, 0x00, 0x00};
        MockMultipartFile mf = new MockMultipartFile("file", "test.mp3", "audio/mpeg", fakeMp3);

        // Establecer un directorio temporal en el servicio
        Path tmp = Files.createTempDirectory("audios-test");
        try {
            java.lang.reflect.Field f = ContenidoService.class.getDeclaredField("audioStoragePath");
            f.setAccessible(true);
            f.set(service, tmp.toString());

            Contenido out = service.storeAudioFile("cid1", mf, "u@u.com");

            assertNotNull(out.getFicheroAudio());
            // Comprueba que el fichero físico existe
            Path stored = tmp.resolve(out.getFicheroAudio().contains("/") ? out.getFicheroAudio().split("/",2)[1] : out.getFicheroAudio());
            assertTrue(Files.exists(stored));
            verify(contenidoDAO).save(any(Contenido.class));
        } finally {
            // limpiar
            Files.walk(tmp).sorted((a,b)->b.compareTo(a)).forEach(p-> { try{ Files.deleteIfExists(p);}catch(Exception e){} });
        }
    }

    @Test
    void storeAudioFile_throws_when_user_not_authenticated() throws Exception {
        Contenido c = baseAudio();
        c.setId("cid1");

        byte[] fakeMp3 = new byte[] {0x49, 0x44, 0x33, 0x00};
        MockMultipartFile mf = new MockMultipartFile("file", "test.mp3", "audio/mpeg", fakeMp3);

        Path tmp = Files.createTempDirectory("audios-test");
        try {
            java.lang.reflect.Field f = ContenidoService.class.getDeclaredField("audioStoragePath");
            f.setAccessible(true);
            f.set(service, tmp.toString());

            assertThrows(AccessDeniedException.class, () -> service.storeAudioFile("cid1", mf, null));
        } finally {
            Files.walk(tmp).sorted((a,b)->b.compareTo(a)).forEach(p-> { try{ Files.deleteIfExists(p);}catch(Exception e){} });
        }
    }

    @Test
    void storeAudioFile_throws_when_file_empty() throws Exception {
        Contenido c = baseAudio();
        c.setId("cid1");

        MockMultipartFile mf = new MockMultipartFile("file", "empty.mp3", "audio/mpeg", new byte[0]);

        Path tmp = Files.createTempDirectory("audios-test");
        try {
            java.lang.reflect.Field f = ContenidoService.class.getDeclaredField("audioStoragePath");
            f.setAccessible(true);
            f.set(service, tmp.toString());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.storeAudioFile("cid1", mf, "u@u.com"));
            assertTrue(ex.getMessage().toLowerCase().contains("archivo vacío"));
        } finally {
            Files.walk(tmp).sorted((a,b)->b.compareTo(a)).forEach(p-> { try{ Files.deleteIfExists(p);}catch(Exception e){} });
        }
    }

    @Test
    void storeAudioFile_throws_when_mime_invalid() throws Exception {
        Contenido c = baseAudio();
        c.setId("cid1");

        byte[] fake = new byte[] {0x00, 0x01, 0x02};
        MockMultipartFile mf = new MockMultipartFile("file", "test.mp3", "application/octet-stream", fake);

        Path tmp = Files.createTempDirectory("audios-test");
        try {
            java.lang.reflect.Field f = ContenidoService.class.getDeclaredField("audioStoragePath");
            f.setAccessible(true);
            f.set(service, tmp.toString());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.storeAudioFile("cid1", mf, "u@u.com"));
            assertTrue(ex.getMessage().toLowerCase().contains("tipo mime"));
        } finally {
            Files.walk(tmp).sorted((a,b)->b.compareTo(a)).forEach(p-> { try{ Files.deleteIfExists(p);}catch(Exception e){} });
        }
    }

    @Test
    void storeAudioFile_throws_when_extension_invalid() throws Exception {
        Contenido c = baseAudio();
        c.setId("cid1");

        byte[] fakeMp3 = new byte[] {0x49, 0x44, 0x33, 0x00};
        MockMultipartFile mf = new MockMultipartFile("file", "test.wav", "audio/mpeg", fakeMp3);

        Path tmp = Files.createTempDirectory("audios-test");
        try {
            java.lang.reflect.Field f = ContenidoService.class.getDeclaredField("audioStoragePath");
            f.setAccessible(true);
            f.set(service, tmp.toString());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.storeAudioFile("cid1", mf, "u@u.com"));
            assertTrue(ex.getMessage().toLowerCase().contains("extensión no válida"));
        } finally {
            Files.walk(tmp).sorted((a,b)->b.compareTo(a)).forEach(p-> { try{ Files.deleteIfExists(p);}catch(Exception e){} });
        }
    }

    @Test
    void storeAudioFile_throws_when_magic_bytes_invalid() throws Exception {
        Contenido c = baseAudio();
        c.setId("cid1");

        byte[] fake = new byte[] {0x00, 0x11, 0x22, 0x33};
        MockMultipartFile mf = new MockMultipartFile("file", "test.mp3", "audio/mpeg", fake);

        Path tmp = Files.createTempDirectory("audios-test");
        try {
            java.lang.reflect.Field f = ContenidoService.class.getDeclaredField("audioStoragePath");
            f.setAccessible(true);
            f.set(service, tmp.toString());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.storeAudioFile("cid1", mf, "u@u.com"));
            assertTrue(ex.getMessage().toLowerCase().contains("no parece ser un mp3"));
        } finally {
            Files.walk(tmp).sorted((a,b)->b.compareTo(a)).forEach(p-> { try{ Files.deleteIfExists(p);}catch(Exception e){} });
        }
    }

    @Test
    void storeAudioFile_throws_when_storagePath_not_set() throws Exception {
        Contenido c = baseAudio();
        c.setId("cid1");
        when(contenidoDAO.findById("cid1")).thenReturn(Optional.of(c));

        byte[] fakeMp3 = new byte[] {0x49, 0x44, 0x33, 0x00};
        MockMultipartFile mf = new MockMultipartFile("file", "test.mp3", "audio/mpeg", fakeMp3);

        // dejar audioStoragePath como null/ vacío
        java.lang.reflect.Field f = ContenidoService.class.getDeclaredField("audioStoragePath");
        f.setAccessible(true);
        f.set(service, "");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.storeAudioFile("cid1", mf, "u@u.com"));
        assertTrue(ex.getMessage().toLowerCase().contains("audio.storage.path"));
    }
}
