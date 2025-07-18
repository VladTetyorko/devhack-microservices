package com.vladte.devhack.common.service.generations;

import com.vladte.devhack.entities.User;

public interface VacancyParsingService {

    void parseVacancyText(String vacancyText, User user);

}
