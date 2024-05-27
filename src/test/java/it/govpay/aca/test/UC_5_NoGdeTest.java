package it.govpay.aca.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.govpay.aca.Application;
import it.govpay.aca.client.api.AcaApi;
import it.govpay.aca.client.api.impl.ApiClient;
import it.govpay.aca.client.gde.EventiApi;
import it.govpay.aca.repository.VersamentoAcaRepository;
import it.govpay.aca.repository.VersamentoRepository;
import it.govpay.aca.test.entity.VersamentoFullEntity;
import it.govpay.aca.test.repository.ApplicazioneRepository;
import it.govpay.aca.test.repository.DominioRepository;
import it.govpay.aca.test.repository.VersamentoFullRepository;
import it.govpay.aca.test.utils.AcaUtils;
import it.govpay.aca.test.utils.VersamentoUtils;


@SpringBootTest(classes = Application.class, properties = {
        "it.govpay.gde.enabled=false"
})
@EnableAutoConfiguration
@DisplayName("Test Invio ACA e GDE")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SpringBatchTest
class UC_5_NoGdeTest {

	@Autowired
	@MockBean(name = "acaApi")
	AcaApi acaApi;

	@Autowired
	EventiApi gdeApi;

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;

	@Autowired
	@Qualifier(value = "acaSenderJob")
	private Job job;

	@Autowired
	VersamentoFullRepository versamentoFullRepository;

	@Autowired
	ApplicazioneRepository applicazioneRepository;

	@Autowired
	DominioRepository dominioRepository;

	@Autowired
	VersamentoAcaRepository versamentoAcaRepository;

	@Autowired
	VersamentoRepository versamentoRepository;

	@Value("${it.govpay.aca.batch.client.baseUrl}")
	String acaBaseUrl;
	
	private void initailizeJobLauncherTestUtils() throws Exception {
		jobLauncherTestUtils.setJob(job);
	}
	
	@AfterEach
    void tearDown() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

	@BeforeEach
	void setUp() throws URISyntaxException, JsonProcessingException {
		MockitoAnnotations.openMocks(this);
		this.versamentoFullRepository.deleteAll();

		Mockito.lenient()
		.when(acaApi.getApiClient()).thenAnswer(new Answer<ApiClient>() {

			@Override
			public ApiClient answer(InvocationOnMock invocation) throws Throwable {
				ApiClient apiClient = new ApiClient();
				apiClient.setBasePath(acaBaseUrl);
				return apiClient;
			}
		});
	}

	@Test
	void TC_01_SendTest_NoGde() throws Exception {
		try {

			VersamentoFullEntity versamentoAcaEntity = VersamentoUtils.creaVersamentoNonEseguito(this.versamentoFullRepository, this.applicazioneRepository, this.dominioRepository);
			this.versamentoFullRepository.save(versamentoAcaEntity);

			Mockito.lenient()
			.when(acaApi.newDebtPositionWithHttpInfo(any()
					)).thenAnswer(new Answer<ResponseEntity<Void>>() {
						@Override
						public ResponseEntity<Void> answer(InvocationOnMock invocation) throws Throwable {
							ResponseEntity<Void> mockResponseEntity = new ResponseEntity<>(AcaUtils.getHeadersCreatedOk(), HttpStatus.CREATED);
							return mockResponseEntity;
						}
					});

			assertEquals(1, this.versamentoFullRepository.count());
			assertEquals(1, this.versamentoAcaRepository.count());
			assertEquals(1, this.versamentoRepository.count());

			initailizeJobLauncherTestUtils();
			JobExecution jobExecution = jobLauncherTestUtils.launchJob();
			assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

			assertEquals(1, this.versamentoFullRepository.count());
			assertEquals(0, this.versamentoAcaRepository.count());
			assertEquals(1, this.versamentoRepository.count());

		} finally {
			this.versamentoFullRepository.deleteAll();
		}
	}
}
