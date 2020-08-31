package org.mifos.mobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.mifos.mobile.R;
import org.mifos.mobile.injection.ApplicationContext;
import org.mifos.mobile.models.accounts.loan.Periods;
import org.mifos.mobile.models.accounts.loan.tableview.Cell;
import org.mifos.mobile.models.accounts.loan.tableview.ColumnHeader;
import org.mifos.mobile.models.accounts.loan.tableview.RowHeader;
import org.mifos.mobile.utils.CurrencyUtil;
import org.mifos.mobile.utils.DateHelper;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Rajan Maurya on 04/03/17.
 */

public class LoanRepaymentScheduleAdapter extends
        AbstractTableAdapter<ColumnHeader, RowHeader, Cell> {

    private String currency = "";
    private double columnWidth = 0;

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Inject
    LoanRepaymentScheduleAdapter(@ApplicationContext Context context) {
        super(context);
    }

    class CellViewHolder extends AbstractViewHolder {

        @BindView(R.id.cell_data)
        TextView tvCell;

        @BindView(R.id.cell_container)
        LinearLayout llCellContainer;

        CellViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public AbstractViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_loan_repayment_schedule, parent, false);
        return new CellViewHolder(view);
    }

    @Override
    public void onBindCellViewHolder(AbstractViewHolder holder, Object cellItemModel,
                                     int columnPosition, int rowPosition) {
        Cell cell = (Cell) cellItemModel;
        Periods period = (Periods) cell.getData();

        // Get the holder to update cell item text
        CellViewHolder viewHolder = (CellViewHolder) holder;

        switch (columnPosition) {
            case 0:
                viewHolder.tvCell.setText(DateHelper.getDateAsString(period.getDueDate()));
                break;
            case 1:
                Double principal = period.getPrincipalOriginalDue();
                if (principal == null) {
                    principal = 0.00;
                }
                viewHolder.tvCell.setText(mContext.getString(R.string.string_and_double,
                        currency, principal));
                break;
            case 2:
                principal = period.getPrincipalLoanBalanceOutstanding();
                if (principal == null) {
                    principal = 0.00;
                }
                viewHolder.tvCell.setText(mContext.getString(R.string.string_and_string,
                        currency, CurrencyUtil.formatCurrency(mContext, principal)));
                break;
            default:
                viewHolder.tvCell.setText("");
        }

        viewHolder.llCellContainer.getLayoutParams().width = (int) columnWidth;
        viewHolder.tvCell.requestLayout();
    }

    class ColumnHeaderViewHolder extends AbstractViewHolder {

        @BindView(R.id.column_header_textView)
        TextView tvColumnHeader;

        @BindView(R.id.column_header_container)
        LinearLayout llColumnHeaderContainer;

        ColumnHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public AbstractViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {

        // Get Column Header xml Layout
        View layout = LayoutInflater.from(mContext).inflate(R.layout
                .column_header_loan_repayment_schedule, parent, false);

        // Create a ColumnHeader ViewHolder
        return new ColumnHeaderViewHolder(layout);
    }

    @Override
    public void onBindColumnHeaderViewHolder(AbstractViewHolder holder,
                                             Object columnHeaderItemModel, int position) {
        ColumnHeader columnHeader = (ColumnHeader) columnHeaderItemModel;

        // Get the holder to update cell item text
        ColumnHeaderViewHolder columnHeaderViewHolder = (ColumnHeaderViewHolder) holder;

        columnHeaderViewHolder.tvColumnHeader.setText(String.valueOf(columnHeader.getData()));

        columnHeaderViewHolder.llColumnHeaderContainer.getLayoutParams().width = (int) columnWidth;
        columnHeaderViewHolder.tvColumnHeader.requestLayout();
    }

    class RowHeaderViewHolder extends AbstractViewHolder {

        @BindView(R.id.row_header_textview)
        TextView tvRowHeader;

        RowHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public AbstractViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {

//         Get Row Header xml Layout
        View layout = LayoutInflater.from(mContext).inflate(R.layout
                .row_header_loan_repayment_schedule, parent, false);

        // Create a Row Header ViewHolder
        return new RowHeaderViewHolder(layout);
//        return null;
    }

    @Override
    public void onBindRowHeaderViewHolder(AbstractViewHolder holder, Object rowHeaderItemModel, int
            position) {
        RowHeader rowHeader = (RowHeader) rowHeaderItemModel;

        // Get the holder to update row header item text
        RowHeaderViewHolder rowHeaderViewHolder = (RowHeaderViewHolder) holder;
        rowHeaderViewHolder.tvRowHeader.setText(String.valueOf(rowHeader.getData()));
    }

    @Override
    public View onCreateCornerView() {
        return LayoutInflater.from(mContext).inflate(R.layout
                .corner_view_loan_repayment_schedule, null, false);
    }

    @Override
    public int getColumnHeaderItemViewType(int columnPosition) {
        // The unique ID for this type of column header item
        // If you have different items for Cell View by X (Column) position,
        // then you should fill this method to be able create different
        // type of CellViewHolder on "onCreateCellViewHolder"
        return 0;
    }

    @Override
    public int getRowHeaderItemViewType(int rowPosition) {
        // The unique ID for this type of row header item
        // If you have different items for Row Header View by Y (Row) position,
        // then you should fill this method to be able create different
        // type of RowHeaderViewHolder on "onCreateRowHeaderViewHolder"
        return 0;
    }

    @Override
    public int getCellItemViewType(int columnPosition) {
        // The unique ID for this type of cell item
        // If you have different items for Cell View by X (Column) position,
        // then you should fill this method to be able create different
        // type of CellViewHolder on "onCreateCellViewHolder"
        return 0;
    }

    public void setColumnWidth(double columnWidth) {
        this.columnWidth = columnWidth;
    }

}