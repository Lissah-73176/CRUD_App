package vistula.edu.pl.CRUD_App;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Route
public class MainView extends VerticalLayout {

    private final CustomerRepository repo;

    private final CustomerEditor editor;

    final Grid<Customer> grid;

    final TextField filter;

    private final Button addNewBtn;

    private final Button sortFirstNameBtn;

    private boolean isAscending = true;

    public MainView(CustomerRepository repo, CustomerEditor editor) {
        this.repo = repo;
        this.editor = editor;
        this.grid = new Grid<>(Customer.class);
        this.filter = new TextField();
        this.addNewBtn = new Button("New customer", VaadinIcon.PLUS.create());

        this.sortFirstNameBtn = new Button("Sort by First Name", VaadinIcon.ARROW_UP.create());

        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn, sortFirstNameBtn);
        add(actions, grid, editor);

        grid.setHeight("300px");

        // --- ADDED "sex" TO THE COLUMNS LIST HERE ---
        grid.setColumns("id", "firstName", "lastName", "sex");

        grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);

        Grid.Column<Customer> firstNameColumn = grid.getColumnByKey("firstName")
                .setComparator((c1, c2) -> {
                    String name1 = c1.getFirstName() != null ? c1.getFirstName() : "";
                    String name2 = c2.getFirstName() != null ? c2.getFirstName() : "";
                    return name1.compareToIgnoreCase(name2);
                });

        filter.setPlaceholder("Filter by last name");

        sortFirstNameBtn.addClickListener(e -> {
            if (isAscending) {
                grid.sort(Collections.singletonList(
                        new GridSortOrder<>(firstNameColumn, SortDirection.ASCENDING)
                ));
                sortFirstNameBtn.setIcon(VaadinIcon.ARROW_UP.create());
            } else {
                grid.sort(Collections.singletonList(
                        new GridSortOrder<>(firstNameColumn, SortDirection.DESCENDING)
                ));
                sortFirstNameBtn.setIcon(VaadinIcon.ARROW_DOWN.create());
            }
            isAscending = !isAscending;
        });

        filter.setValueChangeMode(ValueChangeMode.LAZY);
        filter.addValueChangeListener(e -> listCustomers(e.getValue()));

        grid.asSingleSelect().addValueChangeListener(e -> {
            editor.editCustomer(e.getValue());
        });

        addNewBtn.addClickListener(e -> editor.editCustomer(new Customer("", "", "")));

        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listCustomers(filter.getValue());
        });

        listCustomers(null);
    }

    void listCustomers(String filterText) {
        if (StringUtils.hasText(filterText)) {
            grid.setItems(repo.findByLastNameStartsWithIgnoreCase(filterText));
        } else {
            grid.setItems(repo.findAll());
        }
    }
}