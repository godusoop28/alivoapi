package com.alivos.api.config;

import com.alivos.api.entity.Course;
import com.alivos.api.entity.CourseModule;
import com.alivos.api.entity.CourseStatus;
import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.EnrollmentSource;
import com.alivos.api.entity.EnrollmentStatus;
import com.alivos.api.entity.Lesson;
import com.alivos.api.entity.LessonProgress;
import com.alivos.api.entity.LessonType;
import com.alivos.api.entity.ManualAccess;
import com.alivos.api.entity.ManualAccessStatus;
import com.alivos.api.entity.Purchase;
import com.alivos.api.entity.PurchaseMethod;
import com.alivos.api.entity.PurchaseStatus;
import com.alivos.api.entity.Role;
import com.alivos.api.entity.Settings;
import com.alivos.api.entity.TaskComment;
import com.alivos.api.entity.TaskSubmission;
import com.alivos.api.entity.TaskSubmissionStatus;
import com.alivos.api.entity.User;
import com.alivos.api.entity.UserStatus;
import com.alivos.api.repository.CourseModuleRepository;
import com.alivos.api.repository.CourseRepository;
import com.alivos.api.repository.EnrollmentRepository;
import com.alivos.api.repository.LessonProgressRepository;
import com.alivos.api.repository.LessonRepository;
import com.alivos.api.repository.ManualAccessRepository;
import com.alivos.api.repository.PurchaseRepository;
import com.alivos.api.repository.SettingsRepository;
import com.alivos.api.repository.TaskCommentRepository;
import com.alivos.api.repository.TaskSubmissionRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Idempotent demo-data seeder: safe to run on every startup. Users and course
 * "shells" are upserted by their natural key (email / slug); module/lesson
 * content and the demo purchase/task/manual-access rows are only created the
 * first time (guarded by existence checks) so re-running never duplicates them.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String ASSETS_BASE = "/alivos-assets";
    private static final String COURSE1_SLUG = "descubriendo-su-cuerpo-0-3-meses";
    private static final String COURSE1_DIR = ASSETS_BASE + "/courses/" + COURSE1_SLUG;
    private static final String DEMO_VIDEO_THUMB =
            "https://images.unsplash.com/photo-1555252333-9f8e92e65df9?auto=format&fit=crop&w=800&q=80";

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final PurchaseRepository purchaseRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final ManualAccessRepository manualAccessRepository;
    private final SettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Seed deshabilitado (app.seed.enabled=false)");
            return;
        }
        log.info("Sembrando base de datos de ALIVOS...");

        User admin = upsertUser("admin@alivos.com", "Administradora ALIVOS", null, Role.ADMIN, UserStatus.ACTIVE, "Admin12345!");
        User cliente = upsertUser("cliente@alivos.com", "Cliente Demo", "+52 55 2813 2020", Role.STUDENT, UserStatus.ACTIVE, "Cliente12345!");

        User maria = upsertUser("maria.gonzalez@email.com", "María González", "+52 55 1234 5678", Role.STUDENT, UserStatus.ACTIVE, "Alivos12345!");
        User laura = upsertUser("laura.fernandez@email.com", "Laura Fernández", "+52 55 2345 6789", Role.STUDENT, UserStatus.ACTIVE, "Alivos12345!");
        User ana = upsertUser("ana.rodriguez@email.com", "Ana Rodríguez", "+52 55 3456 7890", Role.STUDENT, UserStatus.ACTIVE, "Alivos12345!");
        upsertUser("carolina.lopez@email.com", "Carolina López", "+52 55 4567 8901", Role.STUDENT, UserStatus.BLOCKED, "Alivos12345!");

        Course course1 = seedCourse1();
        Course course2 = seedCourse2();
        Course course3 = seedCourse3();
        Course course4 = seedCourse4();

        seedClienteDemoData(cliente, admin, course1, course2);
        seedFillerData(maria, laura, ana, course1, course2, course3);

        seedSettings();

        log.info("Seed completado. Admin: admin@alivos.com / Admin12345! — Alumno: cliente@alivos.com / Cliente12345!");
    }

    // ----- Users -----

    private User upsertUser(String email, String name, String phone, Role role, UserStatus status, String rawPassword) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPhone(phone);
            user.setRole(role);
            user.setStatus(status);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            return userRepository.save(user);
        });
    }

    // ----- Course shells -----

    private Course upsertCourseShell(String title, String slug, String ageRange, int price,
                                      String shortDescription, String longDescription,
                                      String coverImage, String bannerImage) {
        return courseRepository.findBySlug(slug).orElseGet(() -> {
            Course course = new Course();
            course.setTitle(title);
            course.setSlug(slug);
            course.setAgeRange(ageRange);
            course.setPrice(price);
            course.setShortDescription(shortDescription);
            course.setLongDescription(longDescription);
            course.setCoverImage(coverImage);
            course.setBannerImage(bannerImage);
            course.setStatus(CourseStatus.PUBLISHED);
            return courseRepository.save(course);
        });
    }

    private boolean hasContent(Course course) {
        return moduleRepository.countByCourseId(course.getId()) > 0;
    }

    private CourseModule addModule(Course course, int order, String title, String description,
                                    String coverImage, String bannerImage) {
        CourseModule module = new CourseModule();
        module.setCourse(course);
        module.setTitle(title);
        module.setDescription(description);
        module.setOrderIndex(order);
        module.setVisible(true);
        module.setCoverImage(coverImage);
        module.setBannerImage(bannerImage);
        return moduleRepository.save(module);
    }

    private void addLesson(CourseModule module, int order, LessonSeed seed) {
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(seed.title());
        lesson.setType(seed.type());
        lesson.setDescription(seed.description());
        lesson.setOrderIndex(order);
        lesson.setDurationMinutes(seed.durationMinutes());
        lesson.setHasMaterial(seed.hasMaterial());
        lesson.setMaterialUrl(seed.materialUrl());
        lesson.setHasTask(seed.hasTask());
        lesson.setTaskDescription(seed.taskDescription());
        lesson.setImageUrl(seed.imageUrl());
        lesson.setPdfUrl(seed.pdfUrl());
        lesson.setAssetType(seed.assetType());
        if (seed.withVimeo()) {
            lesson.setVimeoId("76979871");
            lesson.setVimeoUrl("https://vimeo.com/76979871");
            lesson.setVimeoEmbedUrl("https://player.vimeo.com/video/76979871");
            lesson.setVimeoThumbnail(DEMO_VIDEO_THUMB);
        }
        lessonRepository.save(lesson);
    }

    // ----- Course 1: "Descubriendo su cuerpo" (real content) -----

    private ModuleAssets course1ModuleAssets(int n) {
        String dir = COURSE1_DIR + "/modulo-0" + n;
        String[] coverSuffix = {"bebe-boca-abajo", "bebe-sentado", "bebe-gateo", "bebe-caminando"};
        return new ModuleAssets(
                dir + "/banner-programa-casa-modulo-0" + n + ".png",
                dir + "/portada-modulo-0" + n + "-" + coverSuffix[n - 1] + ".png",
                dir + "/objetivos-modulo-0" + n + ".png",
                dir + "/materiales-modulo-0" + n + ".png",
                dir + "/contenido-extra-modulo-0" + n + ".png",
                dir + "/preguntas-frecuentes-modulo-0" + n + ".png",
                dir + "/actividades-modulo-0" + n + ".pdf",
                dir + "/evaluacion-modulo-0" + n + ".pdf"
        );
    }

    private Course seedCourse1() {
        String[][] themes = {
                {"Módulo 1: Regulación y contacto corporal",
                        "Primeras semanas: organización del sistema nervioso y contacto piel a piel.",
                        "Practica las técnicas de regulación por 3 días y cuéntanos cómo respondió tu bebé. Adjunta una foto o video corto (máx. 1 min)."},
                {"Módulo 2: Control postural y equilibrio",
                        "Sentamos las bases del control de tronco que tu bebé irá dominando en los próximos meses.",
                        "Realiza los ejercicios de base postural y documenta tu experiencia con fotos o video."},
                {"Módulo 3: Preparando el desplazamiento",
                        "Estimulación temprana de los patrones que, más adelante, facilitarán el gateo.",
                        "Practica las activaciones de esta fase y cuéntanos cómo respondió tu bebé."},
                {"Módulo 4: Cierre, integración y siguientes pasos",
                        "Repaso general del curso y una mirada hacia las próximas etapas del desarrollo motor.",
                        "Completa la actividad de cierre y comparte tus observaciones finales."},
        };

        Course course = upsertCourseShell(
                "Descubriendo su cuerpo", COURSE1_SLUG, "0–3 meses", 5500,
                "Acompaña el desarrollo sensorial y corporal de tu bebé en sus primeros meses de vida con guías claras y ejercicios profesionales.",
                "Este curso está diseñado para padres y cuidadores que quieren acompañar activamente el desarrollo de su bebé en la etapa más crítica. Aprenderás técnicas de estimulación sensorial, posturas seguras y actividades que favorecen la organización corporal desde los primeros días, guiadas por el equipo de especialistas de ALIVOS Medicina de Rehabilitación.",
                course1ModuleAssets(1).cover(),
                ASSETS_BASE + "/home/banner-bebes-alivos-horizontal.jpg"
        );

        if (hasContent(course)) {
            return course;
        }

        String[] evaluationImages = {
                COURSE1_DIR + "/evaluacion/imagen-evaluacion-pediatra-01.png",
                COURSE1_DIR + "/evaluacion/imagen-evaluacion-pediatra-02.png",
        };

        for (int i = 0; i < themes.length; i++) {
            int n = i + 1;
            ModuleAssets assets = course1ModuleAssets(n);
            CourseModule module = addModule(course, i, themes[i][0], themes[i][1], assets.cover(), assets.banner());

            int order = 0;
            addLesson(module, order++, new LessonSeed(
                    "Portada del módulo", LessonType.VIDEO, 5,
                    "Presentación visual del " + themes[i][0].toLowerCase() + ".",
                    false, null, false, null, n == 1, assets.cover(), null, "cover"
            ));
            addLesson(module, order++, new LessonSeed(
                    "Objetivos del módulo", LessonType.TEXT, 6, themes[i][1],
                    false, null, false, null, false, assets.objectives(), null, "objectives"
            ));
            addLesson(module, order++, new LessonSeed(
                    "Materiales necesarios", LessonType.TEXT, 3,
                    "Estos son los materiales que vas a necesitar para las actividades de este módulo.",
                    false, null, false, null, false, assets.materials(), null, "materials"
            ));
            addLesson(module, order++, new LessonSeed(
                    "Contenido extra", LessonType.TEXT, 8,
                    "Material complementario con referencias y recursos adicionales para profundizar.",
                    false, null, false, null, false, assets.extra(), null, "extra"
            ));
            addLesson(module, order++, new LessonSeed(
                    "Preguntas frecuentes", LessonType.TEXT, 6,
                    "Respuestas a las preguntas más comunes de las familias que cursaron este módulo.",
                    false, null, false, null, false, assets.faq(), null, "faq"
            ));
            addLesson(module, order++, new LessonSeed(
                    "Actividades del módulo", LessonType.TASK, 20,
                    "Pon en práctica lo aprendido con la guía de actividades de este módulo.",
                    true, assets.activitiesPdf(), true, themes[i][2], false, assets.materials(), assets.activitiesPdf(), "activities"
            ));
            addLesson(module, order++, new LessonSeed(
                    "Evaluación del módulo", LessonType.EVALUATION, 15,
                    "Cuestionario para evaluar los conocimientos adquiridos en este módulo.",
                    true, assets.evaluationPdf(), false, null, false,
                    evaluationImages[i % evaluationImages.length], assets.evaluationPdf(), "evaluation"
            ));

            if (n == 4) {
                String diaryPdf = COURSE1_DIR + "/recursos/diario-de-ejercicios.pdf";
                addLesson(module, order, new LessonSeed(
                        "Diario de ejercicios (recurso descargable)", LessonType.PDF, 5,
                        "Plantilla para llevar el registro de las actividades realizadas durante todo el curso.",
                        true, diaryPdf, false, null, false, null, diaryPdf, "resource"
                ));
            }
        }

        return course;
    }

    // ----- Courses 2–4: simplified content -----

    private Course seedCourse2() {
        Course course = upsertCourseShell(
                "Descubriendo el movimiento", "descubriendo-el-movimiento", "3–6 meses", 5500,
                "Estimula las habilidades motoras de tu bebé en la etapa en que comienza a descubrir el movimiento voluntario.",
                "A los 3-6 meses tu bebé empieza a moverse con más intención. Este curso te enseña cómo estimular esos movimientos tempranos, favorecer el rolado y preparar su cuerpo para las siguientes etapas del desarrollo.",
                ASSETS_BASE + "/home/portada-principal-juego-desarrollo-01.png", null
        );
        if (hasContent(course)) return course;

        CourseModule intro = addModule(course, 0, "Introducción", null, null, null);
        addLesson(intro, 0, LessonSeed.of("Bienvenida al curso", LessonType.VIDEO, 5, "Presentación del curso y del equipo de ALIVOS.", null, null));
        addLesson(intro, 1, LessonSeed.of("Objetivos de la etapa", LessonType.VIDEO, 10, "Qué habilidades esperamos que tu bebé desarrolle en esta etapa.", null, null));

        CourseModule motor = addModule(course, 1, "Desarrollo motor", null, null, null);
        addLesson(motor, 0, new LessonSeed("Preparando el rolado", LessonType.VIDEO, 20,
                "Técnicas para estimular el rolado de manera segura y progresiva.",
                true, null, true, "Practica las técnicas de rolado y documenta el progreso con fotos o un video corto.",
                false, null, null, null));
        addLesson(motor, 1, LessonSeed.of("Juego de manos y pies", LessonType.VIDEO, 15, "Actividades para estimular el descubrimiento de manos y pies.", null, null));
        addLesson(motor, 2, new LessonSeed("Tiempo boca abajo", LessonType.VIDEO, 18,
                "Cómo hacer el tiempo boca abajo de manera divertida y efectiva.",
                true, null, false, null, false, null, null, null));

        return course;
    }

    private Course seedCourse3() {
        Course course = upsertCourseShell(
                "Preparándose para gatear", "preparandose-para-gatear", "6–9 meses", 5500,
                "Guía a tu bebé en la etapa de preparación para el gateo, una de las más importantes para el desarrollo neurológico.",
                "El gateo es una etapa fundamental que muchos bebés saltan. Este curso explica por qué es importante y cómo estimular todos los patrones de movimiento que preparan a tu bebé para gatear correctamente.",
                ASSETS_BASE + "/home/portada-principal-juego-desarrollo-02.png", null
        );
        if (hasContent(course)) return course;

        CourseModule intro = addModule(course, 0, "Introducción", null, null, null);
        addLesson(intro, 0, LessonSeed.of("Importancia del gateo", LessonType.VIDEO, 12, "Por qué el gateo es fundamental para el desarrollo neurológico.", null, null));

        CourseModule prep = addModule(course, 1, "Preparación para el gateo", null, null, null);
        addLesson(prep, 0, new LessonSeed("Posición en cuatro puntos", LessonType.VIDEO, 20,
                "Cómo ayudar a tu bebé a mantener la posición de cuatro puntos.",
                true, null, false, null, false, null, null, null));
        addLesson(prep, 1, new LessonSeed("Traslado de peso", LessonType.VIDEO, 18,
                "Ejercicios para trabajar el traslado de peso lateral y anterior.",
                false, null, true, "Practica el traslado de peso y observa la respuesta de tu bebé. Cuéntanos tus observaciones.",
                false, null, null, null));

        return course;
    }

    private Course seedCourse4() {
        Course course = upsertCourseShell(
                "Aprendiendo a caminar", "aprendiendo-a-caminar", "9–12 meses", 5500,
                "Acompaña a tu bebé en los primeros pasos, entendiendo cada etapa del proceso y cómo estimularla de manera segura.",
                "Caminar es el logro más esperado. Este curso te guía por todas las etapas previas al primer paso independiente y te enseña a acompañar el proceso sin apresurarlo, respetando los tiempos de tu bebé.",
                ASSETS_BASE + "/home/banner-bebes-alivos.png", null
        );
        if (hasContent(course)) return course;

        CourseModule intro = addModule(course, 0, "Introducción", null, null, null);
        addLesson(intro, 0, LessonSeed.of("El camino hacia el primer paso", LessonType.VIDEO, 10, "Resumen de todas las etapas previas a caminar.", null, null));

        CourseModule bipedestacion = addModule(course, 1, "Bipedestación", null, null, null);
        addLesson(bipedestacion, 0, new LessonSeed("Aprendiendo a pararse", LessonType.VIDEO, 22,
                "Técnicas para estimular la posición de pie con apoyo.",
                true, null, false, null, false, null, null, null));
        addLesson(bipedestacion, 1, new LessonSeed("Primeros pasos con apoyo", LessonType.VIDEO, 25,
                "Cómo guiar los primeros pasos de manera segura.",
                true, null, true, "Documenta los avances de tu bebé con un video corto.",
                false, null, null, null));

        return course;
    }

    // ----- Demo data for cliente@alivos.com -----

    private void seedClienteDemoData(User cliente, User admin, Course course1, Course course2) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(cliente.getId(), course1.getId())
                .orElseGet(() -> {
                    Enrollment e = new Enrollment();
                    e.setUser(cliente);
                    e.setCourse(course1);
                    e.setSource(EnrollmentSource.PURCHASE);
                    return e;
                });
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgress(45);
        enrollmentRepository.save(enrollment);

        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(course1.getId());
        for (CourseModule module : modules) {
            boolean shouldComplete = module.getTitle().startsWith("Módulo 1")
                    || (module.getTitle().startsWith("Módulo 2"));
            if (!shouldComplete) continue;

            for (Lesson lesson : lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId())) {
                if (module.getTitle().startsWith("Módulo 2") && "Evaluación del módulo".equals(lesson.getTitle())) {
                    continue;
                }
                LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(cliente.getId(), lesson.getId())
                        .orElseGet(() -> {
                            LessonProgress p = new LessonProgress();
                            p.setUser(cliente);
                            p.setLesson(lesson);
                            return p;
                        });
                progress.setCompleted(true);
                progress.setCompletedAt(Instant.now());
                lessonProgressRepository.save(progress);
            }
        }

        Optional<Lesson> modulo2Actividades = modules.stream()
                .filter(m -> m.getTitle().startsWith("Módulo 2"))
                .flatMap(m -> lessonRepository.findByModuleIdOrderByOrderIndexAsc(m.getId()).stream())
                .filter(l -> "Actividades del módulo".equals(l.getTitle()))
                .findFirst();

        modulo2Actividades.ifPresent(lesson -> {
            boolean isNewTask = taskSubmissionRepository.findFirstByUserIdAndLessonId(cliente.getId(), lesson.getId()).isEmpty();
            TaskSubmission task = taskSubmissionRepository.findFirstByUserIdAndLessonId(cliente.getId(), lesson.getId())
                    .orElseGet(() -> {
                        TaskSubmission t = new TaskSubmission();
                        t.setUser(cliente);
                        t.setLesson(lesson);
                        return t;
                    });
            task.setStatus(TaskSubmissionStatus.DELIVERED);
            task = taskSubmissionRepository.save(task);
            if (isNewTask) {
                TaskComment comment = new TaskComment();
                comment.setTaskSubmission(task);
                comment.setAuthor(cliente);
                comment.setText("Hicimos los ejercicios de control postural durante 3 días. Al principio mi bebé estaba inquieto, pero fue relajándose con cada sesión.");
                taskCommentRepository.save(comment);
            }
        });

        boolean hasDemoPurchase = purchaseRepository.findByUserIdOrderByCreatedAtDesc(cliente.getId()).stream()
                .anyMatch(p -> "MP-DEMO-00012345".equals(p.getPaymentId()));
        if (!hasDemoPurchase) {
            Purchase purchase = new Purchase();
            purchase.setUser(cliente);
            purchase.setCourse(course1);
            purchase.setAmount(course1.getPrice());
            purchase.setStatus(PurchaseStatus.PAID);
            purchase.setMethod(PurchaseMethod.MERCADO_PAGO);
            purchase.setPaymentId("MP-DEMO-00012345");
            purchaseRepository.save(purchase);
        }

        boolean hasDemoAccess = manualAccessRepository.findAllByOrderByCreatedAtDesc().stream()
                .anyMatch(a -> a.getEmail().equals(cliente.getEmail()) && a.getCourse().getId().equals(course2.getId()));
        if (!hasDemoAccess) {
            ManualAccess access = new ManualAccess();
            access.setEmail(cliente.getEmail());
            access.setUser(cliente);
            access.setCourse(course2);
            access.setGrantedBy(admin);
            access.setReason("Cortesía de bienvenida — cuenta demo");
            access.setStatus(ManualAccessStatus.ACTIVE);
            manualAccessRepository.save(access);
        }

        Enrollment enrollment2 = enrollmentRepository.findByUserIdAndCourseId(cliente.getId(), course2.getId())
                .orElseGet(() -> {
                    Enrollment e = new Enrollment();
                    e.setUser(cliente);
                    e.setCourse(course2);
                    e.setSource(EnrollmentSource.MANUAL);
                    e.setProgress(0);
                    return e;
                });
        enrollment2.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentRepository.save(enrollment2);
    }

    // ----- Filler data for the extra demo students -----

    private void seedFillerData(User maria, User laura, User ana, Course course1, Course course2, Course course3) {
        upsertEnrollment(maria, course1, EnrollmentSource.PURCHASE, 80);
        upsertPurchaseIfAbsent(maria, course1, PurchaseStatus.PAID, PurchaseMethod.MERCADO_PAGO, "MP-DEMO-00023456");

        upsertEnrollment(laura, course2, EnrollmentSource.PURCHASE, 30);
        upsertPurchaseIfAbsent(laura, course2, PurchaseStatus.PAID, PurchaseMethod.MERCADO_PAGO, "MP-DEMO-00034567");

        upsertEnrollment(ana, course3, EnrollmentSource.TRANSFER, 10);
        boolean hasPendingTransfer = purchaseRepository.findByUserIdOrderByCreatedAtDesc(ana.getId()).stream()
                .anyMatch(p -> p.getCourse().getId().equals(course3.getId()) && p.getMethod() == PurchaseMethod.TRANSFER);
        if (!hasPendingTransfer) {
            Purchase purchase = new Purchase();
            purchase.setUser(ana);
            purchase.setCourse(course3);
            purchase.setAmount(course3.getPrice());
            purchase.setStatus(PurchaseStatus.PENDING);
            purchase.setMethod(PurchaseMethod.TRANSFER);
            purchaseRepository.save(purchase);
        }
    }

    private void upsertEnrollment(User user, Course course, EnrollmentSource source, int progress) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId())
                .orElseGet(() -> {
                    Enrollment e = new Enrollment();
                    e.setUser(user);
                    e.setCourse(course);
                    return e;
                });
        enrollment.setSource(source);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgress(progress);
        enrollmentRepository.save(enrollment);
    }

    private void upsertPurchaseIfAbsent(User user, Course course, PurchaseStatus status, PurchaseMethod method, String paymentId) {
        boolean exists = purchaseRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .anyMatch(p -> paymentId.equals(p.getPaymentId()));
        if (exists) return;

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setCourse(course);
        purchase.setAmount(course.getPrice());
        purchase.setStatus(status);
        purchase.setMethod(method);
        purchase.setPaymentId(paymentId);
        purchaseRepository.save(purchase);
    }

    // ----- Settings -----

    private void seedSettings() {
        if (settingsRepository.existsById(Settings.SINGLETON_ID)) {
            return;
        }
        Settings settings = new Settings();
        settings.setId(Settings.SINGLETON_ID);
        settings.setWhatsapp("5213329421890");
        settings.setEmail("info@alivosestimulacion.com");
        settings.setInstagram("@alivosestimulacion");
        settings.setFacebook("alivosestimulacion");
        settings.setWebsite("alivosestimulacion.com");
        settings.setBrandName("ALIVOS Medicina de Rehabilitación");
        settingsRepository.save(settings);
    }
}
