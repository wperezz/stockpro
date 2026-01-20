package com.example.estoqueloja.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.estoqueloja.data.dao.VendaResumoRow;
import com.example.estoqueloja.data.dao.VendaTotaisRow;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.databinding.ActivityVendasBinding;
import com.example.estoqueloja.ui.adapter.VendasAdapter;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class VendasActivity extends AppCompatActivity {

    private ActivityVendasBinding b;
    private VendasAdapter adapter;

    private boolean modoMes = false;
    private final Calendar calRef = Calendar.getInstance();

    private final SimpleDateFormat dfDia = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat dfMes = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVendasBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());

        TextView titulo = findViewById(com.example.estoqueloja.R.id.txtTitulo);
        titulo.setText("Vendas");

        findViewById(com.example.estoqueloja.R.id.btnVoltar).setOnClickListener(v -> finish());

        adapter = new VendasAdapter(venda -> {
            Intent it = new Intent(this, VendaDetalheActivity.class);
            it.putExtra("vendaId", venda.vendaId);
            startActivity(it);
        });

        b.recyclerVendas.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerVendas.setAdapter(adapter);

        // default: DIA (hoje)
        modoMes = false;
        atualizarTextoPeriodo();

        b.rbDia.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                modoMes = false;
                b.edPeriodo.setHint("Selecione a data");
                atualizarTextoPeriodo();
                carregarFiltrado();
            }
        });

        b.rbMes.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                modoMes = true;
                b.edPeriodo.setHint("Selecione o mês");
                // força dia 1 para não confundir
                calRef.set(Calendar.DAY_OF_MONTH, 1);
                atualizarTextoPeriodo();
                carregarFiltrado();
            }
        });

        b.edPeriodo.setOnClickListener(v -> abrirPicker());
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarFiltrado();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void abrirPicker() {
        if (!modoMes) {
            // DIA: MaterialDatePicker
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Selecionar dia")
                    .setSelection(calRef.getTimeInMillis())
                    .build();

            picker.addOnPositiveButtonClickListener(ms -> {
                calRef.setTimeInMillis(ms);
                atualizarTextoPeriodo();
                carregarFiltrado();
            });

            picker.show(getSupportFragmentManager(), "pickerDia");
        } else {
            // MÊS: DatePickerDialog (usa só mês/ano)
            int y = calRef.get(Calendar.YEAR);
            int m = calRef.get(Calendar.MONTH);

            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calRef.set(Calendar.YEAR, year);
                calRef.set(Calendar.MONTH, month);
                calRef.set(Calendar.DAY_OF_MONTH, 1);
                atualizarTextoPeriodo();
                carregarFiltrado();
            }, y, m, 1).show();
        }
    }

    private void atualizarTextoPeriodo() {
        if (!modoMes) {
            b.edPeriodo.setText(dfDia.format(calRef.getTime()));
        } else {
            b.edPeriodo.setText(dfMes.format(calRef.getTime()));
        }
    }

    private void carregarFiltrado() {
        final long ini;
        final long fim;

        Calendar cIni = (Calendar) calRef.clone();
        Calendar cFim;

        if (!modoMes) {
            // dia: [00:00, +1 dia)
            cIni.set(Calendar.HOUR_OF_DAY, 0);
            cIni.set(Calendar.MINUTE, 0);
            cIni.set(Calendar.SECOND, 0);
            cIni.set(Calendar.MILLISECOND, 0);

            cFim = (Calendar) cIni.clone();
            cFim.add(Calendar.DAY_OF_MONTH, 1);

        } else {
            // mês: [1º dia 00:00, 1º dia do próximo mês)
            cIni.set(Calendar.DAY_OF_MONTH, 1);
            cIni.set(Calendar.HOUR_OF_DAY, 0);
            cIni.set(Calendar.MINUTE, 0);
            cIni.set(Calendar.SECOND, 0);
            cIni.set(Calendar.MILLISECOND, 0);

            cFim = (Calendar) cIni.clone();
            cFim.add(Calendar.MONTH, 1);
        }

        ini = cIni.getTimeInMillis();
        fim = cFim.getTimeInMillis();

        AppExecutors.get().io().execute(() -> {
            List<VendaResumoRow> lista =
                    DbProvider.get(this).movDao().listarVendasAgrupadasPeriodo(ini, fim);

            VendaTotaisRow tot =
                    DbProvider.get(this).movDao().totaisVendasPeriodo(ini, fim);

            if (tot == null) {
                tot = new VendaTotaisRow();
                tot.total = 0;
                tot.lucro = 0;
            }

            VendaTotaisRow finalTot = tot;

            AppExecutors.get().main().post(() -> {
                adapter.setItens(lista);

                b.txtTotalPeriodo.setText(String.format(Locale.getDefault(), "Total: R$ %.2f", finalTot.total));
                b.txtLucroPeriodo.setText(String.format(Locale.getDefault(), "Lucro: R$ %.2f", finalTot.lucro));
            });
        });
    }
}
