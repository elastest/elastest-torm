package io.elastest.etm.test.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.elastest.etm.dao.SutExecutionRepository;
import io.elastest.etm.dao.SutRepository;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.Enums.ProtocolEnum;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.InstrumentedByEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.service.SutService;
import io.elastest.etm.test.extensions.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SutServiceTest {

    @Mock
    public SutRepository sutRepository;

    @Mock
    public SutExecutionRepository sutExecutionRepository;

    @InjectMocks
    public SutService sutService;

    Optional<SutSpecification> sutOptional;
    Optional<SutExecution> sutExecOptional;

    public Long sutId;
    public SutSpecification sut;

    public Long sutExecId;
    public SutExecution sutExec;

    @BeforeEach
    public void setUp() {
        sutId = 1L;
        sut = new SutSpecification(sutId, "sut name", "sut Specification",
                "sut desc", new Project(), null, SutTypeEnum.MANAGED, false,
                null, InstrumentedByEnum.ELASTEST, null,
                ManagedDockerType.IMAGE, CommandsOptionEnum.DEFAULT, ProtocolEnum.HTTP);
        sutExecId = 1L;
        sutExec = new SutExecution(sutExecId, sut, null, null);

        sutOptional = Optional.of(sut);
        sutExecOptional = Optional.of(sutExec);
    }

    @Test
    public void testUndeploySut() {
        when(sutRepository.findById(sutId)).thenReturn(sutOptional);
        when(sutExecutionRepository.findByIdAndSutSpecification(sutExecId, sut))
                .thenReturn(sutExec);

        sutService.undeploySut(sutId, sutExecId);
        assertTrue(sutExec.getDeployStatus()
                .equals(SutExecution.DeployStatusEnum.UNDEPLOYED));

    }

    @Test
    public void testCreateSutExecutionById() {
        when(sutRepository.findById(sutId)).thenReturn(sutOptional);
        when(sutExecutionRepository.save(Mockito.any(SutExecution.class)))
                .thenReturn(sutExec);

        SutExecution sutExec = sutService.createSutExecutionById(sutId);
        assertTrue(sutExec.getSutSpecification().getId() == this.sut.getId());
    }

    @Test
    public void testCreateSutExecutionBySut() {
        when(sutExecutionRepository.save(Mockito.any(SutExecution.class)))
                .thenReturn(sutExec);

        SutExecution sutExec = sutService.createSutExecutionBySut(sut);
        assertTrue(sutExec.getSutSpecification().getId() == this.sut.getId());

    }

    @Test
    public void testDeleteSutExec() {
        when(sutExecutionRepository.findById(sutExecId))
                .thenReturn(sutExecOptional);

        sutService.deleteSutExec(sutExec.getId());
        assertTrue(true);
    }

    @Test
    public void testGetAllSutExecBySutId() {
        List<SutExecution> sutExecutions = new ArrayList<>();
        sutExecutions.add(new SutExecution(2L, sut, null, null));
        sutExecutions.add(new SutExecution(3L, sut, null, null));

        when(sutRepository.findById(sutId)).thenReturn(sutOptional);
        when(sutExecutionRepository.findAll()).thenReturn(sutExecutions);

        List<SutExecution> sutExecutionsResult = sutService
                .getAllSutExecBySutId(sutId);
        assertTrue(sutExecutionsResult.size() == 2);
    }

    @Test
    public void testGetSutExecutionById() {
        when(sutExecutionRepository.findById(sutExecId))
                .thenReturn(sutExecOptional);

        SutExecution sutExecLocal = sutService.getSutExecutionById(sutExecId);
        assertTrue(sutExecLocal.getId() == sutExecId);
    }

    @Test
    public void testModifySutExec() {
        when(sutExecutionRepository.findById(sutExecId))
                .thenReturn(sutExecOptional);
        when(sutExecutionRepository.save(Mockito.any(SutExecution.class)))
                .thenReturn(sutExec);

        SutExecution sutExecResult = sutService.modifySutExec(sutExec);
        assertTrue(sutExecResult.getId() == sutExec.getId());
    }

}
