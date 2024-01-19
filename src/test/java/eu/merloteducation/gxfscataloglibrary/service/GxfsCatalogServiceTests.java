package eu.merloteducation.gxfscataloglibrary.service;

import eu.merloteducation.gxfscataloglibrary.config.GxfsCatalogLibConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
public class GxfsCatalogServiceTests {

    @Autowired
    private GxfsCatalogService gxfsCatalogService;

    @MockBean
    private GxfsCatalogAuthService gxfsCatalogAuthService;

    @MockBean
    private GxfsCatalogClient gxfsCatalogClient;

    @MockBean
    private GxfsSignerService gxfsSignerService;

    @MockBean
    private GxfsCatalogLibConfig gxfsCatalogLibConfig;


    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(gxfsCatalogService, "gxfsCatalogClient", new GxfsCatalogClientFake());
        ReflectionTestUtils.setField(gxfsCatalogService, "gxfsSignerService", gxfsSignerService);
    }

    @Test
    void test()  {
        System.out.println(gxfsCatalogService.revokeSelfDescriptionByHash("1234"));
    }

}
