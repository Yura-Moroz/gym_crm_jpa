package com.yuramoroz.spring_crm_system.service;

import com.yuramoroz.spring_crm_system.entity.Trainee;
import com.yuramoroz.spring_crm_system.repository.TraineeDao;
import com.yuramoroz.spring_crm_system.service.impl.TraineeServiceImpl;
import com.yuramoroz.spring_crm_system.validation.PasswordManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee trainee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        trainee = Trainee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .address("123 Street")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void saveTraineeWithMultiParam_shouldSaveTraineeSuccessfully() {

        when(traineeDao.ifExistByUsername(anyString())).thenReturn(false);
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        Trainee savedTrainee = traineeService.save(
                "Matt", "Watson", "qwerty123", "456 Street", LocalDate.of(1980, 6, 19));

        verify(traineeDao, times(1)).ifExistByUsername(anyString());
        verify(traineeDao, times(1)).save(any(Trainee.class));
    }

    @Test
    void saveTraineeWithOneParam_shouldSaveTraineeSuccessfully() {
        when(traineeDao.ifExistByUsername(anyString())).thenReturn(false);
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        Trainee savedTrainee = traineeService.save(trainee);

        verify(traineeDao, times(1)).ifExistByUsername(anyString());
        verify(traineeDao, times(1)).save(any(Trainee.class));
    }

    @Test
    void findByIdTest_withExistingTrainee() {
        when(traineeDao.getById(trainee.getId())).thenReturn(Optional.of(trainee));

        Trainee resultTrainee = traineeService.getById(trainee.getId()).get();

        verify(traineeDao, times(1)).getById(trainee.getId());
    }

    @Test
    void findByUsernameTest_ShouldReturnExistingUserByUsername() {
        when(traineeDao.getByUsername(trainee.getUserName())).thenReturn(Optional.of(trainee));

        Trainee resultTrainee = traineeService.getByUsername(trainee.getUserName()).get();

        verify(traineeDao, times(1)).getByUsername(trainee.getUserName());
    }

    @Test
    void deleteTraineeByUsernameTest(){
        when(traineeDao.ifExistByUsername(trainee.getUserName())).thenReturn(true);
        when(traineeDao.getByUsername(trainee.getUserName())).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername(trainee.getUserName());

        verify(traineeDao, times(1)).ifExistByUsername(trainee.getUserName());
        verify(traineeDao, times(1)).getByUsername(trainee.getUserName());
    }

    @Test
    void deleteTraineeByUsername_shouldLogWarningIfUserNotFound() {
        String username = "@nonexistent";

        when(traineeDao.ifExistByUsername(username)).thenReturn(false);

        traineeService.deleteByUsername(username);

        verify(traineeDao, never()).delete(any());
    }

    @Test
    void deleteTraineeByUserArgument() {
        traineeService.delete(trainee);

        verify(traineeDao, times(1)).delete(trainee);
    }

    @Test
    void updateTraineeTest() {
        when(traineeDao.ifExistById(trainee.getId())).thenReturn(true);
        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee updatedTrainee = traineeService.update(trainee);

        assertNotNull(updatedTrainee.getId());
        assertEquals(updatedTrainee.getFirstName(), trainee.getFirstName());
        assertEquals(updatedTrainee.getLastName(), trainee.getLastName());
        assertEquals(updatedTrainee.getAddress(), trainee.getAddress());
        assertEquals(updatedTrainee.getDateOfBirth(), trainee.getDateOfBirth());
        assertEquals(updatedTrainee.getUserName(), trainee.getUserName());
        assertEquals(updatedTrainee.getPassword(), trainee.getPassword());
        assertEquals(updatedTrainee.getTrainings(), trainee.getTrainings());
        verify(traineeDao, times(1)).ifExistById(trainee.getId());
        verify(traineeDao, times(1)).update(trainee);
    }

    @Test
    void activateTraineeProfileTest() {
        trainee.setActive(false);

        traineeService.activate(trainee);

        assertTrue(trainee.isActive());
    }

    @Test
    void deactivateTraineeProfileTest() {
        trainee.setActive(true);

        traineeService.deactivate(trainee);

        assertFalse(trainee.isActive());
    }

    @Test
    void changeTraineeProfilePasswordTest() {
        String newPassword = "pass123";
        String oldPassword = trainee.getPassword();

        try (MockedStatic<PasswordManager> mockedStatic = Mockito.mockStatic(PasswordManager.class)) {
            mockedStatic.when(() -> PasswordManager.verify(newPassword)).thenReturn(true);
            mockedStatic.when(() -> PasswordManager.ifPasswordMatches(oldPassword, trainee.getPassword())).thenReturn(true);
            mockedStatic.when(() -> PasswordManager.hashPassword(newPassword)).thenReturn(newPassword);
            when(traineeDao.ifExistById(trainee.getId())).thenReturn(true);
            when(traineeDao.update(trainee)).thenReturn(trainee);

            traineeService.changePassword(trainee, oldPassword, newPassword);

            assertEquals(trainee.getPassword(), newPassword);
            verify(traineeDao, times(2)).ifExistById(trainee.getId());
            verify(traineeDao, times(1)).update(trainee);
            mockedStatic.verify(() -> PasswordManager.hashPassword(newPassword), times(1));
            mockedStatic.verify(() -> PasswordManager.ifPasswordMatches(oldPassword, oldPassword), times(1));
            mockedStatic.verify(() -> PasswordManager.verify(newPassword), times(1));
        }
    }
}
