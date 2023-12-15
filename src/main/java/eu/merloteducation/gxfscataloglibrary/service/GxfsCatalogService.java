package eu.merloteducation.gxfscataloglibrary.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GxfsCatalogService {
    @Autowired
    private GxfsCatalogClient gxfsCatalogClient;

    public void test() {
        this.gxfsCatalogClient.todo();
    }
}
