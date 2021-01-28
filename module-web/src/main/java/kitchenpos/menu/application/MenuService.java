package kitchenpos.menu.application;


import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.menu.domain.MenuProducts;
import kitchenpos.menu.dto.MenuProductRequest;
import kitchenpos.menu.dto.MenuRequest;
import kitchenpos.menu.dto.MenuResponse;
import kitchenpos.menu.repository.MenuGroupRepository;
import kitchenpos.menu.repository.MenuRepository;
import kitchenpos.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final ProductRepository productRepository;

    public MenuService(MenuRepository menuRepository,
                       MenuGroupRepository menuGroupRepository,
                       ProductRepository productRepository
    ) {
        this.menuRepository = menuRepository;
        this.menuGroupRepository = menuGroupRepository;
        this.productRepository = productRepository;
    }

    public MenuResponse create(final MenuRequest menuRequest) {
        MenuGroup menuGroup = menuGroupRepository.findById(menuRequest.getMenuGroupId()).orElseThrow(IllegalArgumentException::new);
        Map<Long, Long> menuProductDto = getMenuProductDto(menuRequest);
        List<Long> productIds = new ArrayList<>(menuProductDto.keySet());

        List<MenuProduct> menuProducts = productRepository.findAllById(productIds)
                .stream()
                .map(product -> new MenuProduct(product, menuProductDto.getOrDefault(product.getId(), 0L)))
                .collect(Collectors.toList());

        Menu menu = new Menu(menuRequest.getName(), menuRequest.getPrice(), menuGroup, new MenuProducts(menuProducts));

        final Menu savedMenu = menuRepository.save(menu);

        return MenuResponse.of(savedMenu);
    }

    public List<MenuResponse> list() {
        final List<Menu> menus = menuRepository.findAll();

        return menus.stream().map(menu ->MenuResponse.of(menu))
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getMenuProductDto(MenuRequest menuRequest) {
        return menuRequest.getMenuProducts()
                .stream()
                .collect(Collectors.toMap(MenuProductRequest::getProductId, MenuProductRequest::getQuantity));
    }
}
