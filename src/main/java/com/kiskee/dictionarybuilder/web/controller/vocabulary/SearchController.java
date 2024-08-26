package com.kiskee.dictionarybuilder.web.controller.vocabulary;

import com.kiskee.dictionarybuilder.repository.vocabulary.projections.WordProjection;
import com.kiskee.dictionarybuilder.service.vocabulary.search.SearchService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public List<WordProjection> search(@RequestParam String searchWord) {
        return searchService.search(searchWord);
    }
}
