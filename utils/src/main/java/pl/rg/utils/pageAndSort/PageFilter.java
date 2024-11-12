package pl.rg.utils.pageAndSort;

import java.util.List;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;

public interface PageFilter<T> {

  List<T> getFiltered(List<Filter> filters);

  MifidPage getPage(List<Filter> filters, Page page);
}
