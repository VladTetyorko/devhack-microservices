package com.vladte.devhack.parser.service.parser;

import com.vladte.devhack.parser.entities.QueryParameters;
import com.vladte.devhack.parser.service.provider.VacancyScrappingService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class VacancyScrappingSchedulerTest {

    @Test
    public void whenEnabled_shouldCallScrappingForAllProviders_withExpectedParams() throws Exception {
        VacancyScrappingService service = Mockito.mock(VacancyScrappingService.class);
        when(service.scrapVacancies(Mockito.anyString(), any(QueryParameters.class)))
                .thenReturn(Collections.emptyList());

        VacancyScrappingScheduler scheduler = new VacancyScrappingScheduler(service);
        setEnabled(scheduler, true);

        scheduler.parseAllSources();

        // Verify called for each provider once
        verify(service, times(1)).scrapVacancies(eq("djinni"), any(QueryParameters.class));
        verify(service, times(1)).scrapVacancies(eq("dou"), any(QueryParameters.class));
        verify(service, times(1)).scrapVacancies(eq("linkedin"), any(QueryParameters.class));

        // Capture one of the calls to check params
        ArgumentCaptor<QueryParameters> paramsCaptor = ArgumentCaptor.forClass(QueryParameters.class);
        verify(service, times(1)).scrapVacancies(eq("dou"), paramsCaptor.capture());
        QueryParameters qp = paramsCaptor.getValue();
        Assert.assertNotNull(qp, "QueryParameters should not be null");
        Assert.assertEquals(qp.getMaxClicks(), 3, "Scheduler should set maxClicks=3");
        Assert.assertEquals(qp.getTopic(), "Java", "Scheduler should set topic=Java");
        Assert.assertTrue(qp.getUrl() != null && qp.getUrl().startsWith("https://"), "URL should be set");
    }

    @Test
    public void whenDisabled_shouldNotCallScrapping() throws Exception {
        VacancyScrappingService service = Mockito.mock(VacancyScrappingService.class);
        when(service.scrapVacancies(Mockito.anyString(), any(QueryParameters.class)))
                .thenReturn(List.of());

        VacancyScrappingScheduler scheduler = new VacancyScrappingScheduler(service);
        setEnabled(scheduler, false);

        scheduler.parseAllSources();

        Mockito.verifyNoInteractions(service);
    }

    @Test
    public void onStartup_whenEnabled_shouldTriggerParsingOnce() throws Exception {
        VacancyScrappingService service = Mockito.mock(VacancyScrappingService.class);
        when(service.scrapVacancies(Mockito.anyString(), any(QueryParameters.class)))
                .thenReturn(Collections.emptyList());

        VacancyScrappingScheduler scheduler = new VacancyScrappingScheduler(service);
        setEnabled(scheduler, true);

        scheduler.onApplicationReady();

        verify(service, times(1)).scrapVacancies(eq("djinni"), any(QueryParameters.class));
        verify(service, times(1)).scrapVacancies(eq("dou"), any(QueryParameters.class));
        verify(service, times(1)).scrapVacancies(eq("linkedin"), any(QueryParameters.class));
    }

    @Test
    public void onStartup_whenDisabled_shouldNotTriggerParsing() throws Exception {
        VacancyScrappingService service = Mockito.mock(VacancyScrappingService.class);
        VacancyScrappingScheduler scheduler = new VacancyScrappingScheduler(service);
        setEnabled(scheduler, false);

        scheduler.onApplicationReady();

        Mockito.verifyNoInteractions(service);
    }

    private static void setEnabled(VacancyScrappingScheduler scheduler, boolean enabled) throws Exception {
        Field f = VacancyScrappingScheduler.class.getDeclaredField("parserEnabled");
        f.setAccessible(true);
        f.setBoolean(scheduler, enabled);
    }
}
