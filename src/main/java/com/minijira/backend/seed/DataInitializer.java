package com.minijira.backend.seed;

import com.minijira.backend.model.Project;
import com.minijira.backend.model.Task;
import com.minijira.backend.model.TaskStatus;
import com.minijira.backend.model.User;
import com.minijira.backend.repository.ProjectRepository;
import com.minijira.backend.repository.TaskRepository;
import com.minijira.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Datos ya inicializados, saltando seed.");
            return;
        }

        log.info("Insertando datos iniciales...");

        // Usuario inicial
        User user = userRepository.save(User.builder()
                .name("Ana Lopez")
                .email("ana.lopez@minijira.com")
                .build());

        // Proyecto inicial
        Project project = projectRepository.save(Project.builder()
                .name("MiniJira MVP")
                .description("Proyecto principal para el desarrollo del MVP de MiniJira")
                .build());

        // Tarea TODO
        taskRepository.save(Task.builder()
                .title("Configurar CI/CD")
                .description("Configurar pipeline de integración y despliegue continuo con GitHub Actions")
                .status(TaskStatus.TODO)
                .storyPoints(5)
                .estimatedHours(8.0)
                .startDate(LocalDate.now().plusDays(7))
                .endDate(LocalDate.now().plusDays(10))
                .assignedUser(user)
                .project(project)
                .build());

        // Tarea IN_PROGRESS
        taskRepository.save(Task.builder()
                .title("Implementar API REST")
                .description("Desarrollar los endpoints REST del backend con Spring Boot y JPA")
                .status(TaskStatus.IN_PROGRESS)
                .storyPoints(13)
                .estimatedHours(16.0)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .assignedUser(user)
                .project(project)
                .build());

        // Tarea DONE
        taskRepository.save(Task.builder()
                .title("Diseño de base de datos")
                .description("Modelado de entidades y relaciones en el esquema de la base de datos")
                .status(TaskStatus.DONE)
                .storyPoints(3)
                .estimatedHours(4.0)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().minusDays(3))
                .assignedUser(user)
                .project(project)
                .build());

        log.info("Seed completado: 1 usuario, 1 proyecto, 3 tareas.");
    }
}
