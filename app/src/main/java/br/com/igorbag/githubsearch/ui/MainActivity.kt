package br.com.igorbag.githubsearch.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var etUsername: EditText
    lateinit var btnConfirmar: Button
    lateinit var rvRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        getAllReposByUserName()
        setupListeners()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        etUsername = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        rvRepositories = findViewById(R.id.rv_lista_repositories)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            username = etUsername.text.toString()
            if (username.isNotEmpty()) {
                getAllReposByUserName()
                saveUserLocal()
            }
        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        val preferences = getPreferences(MODE_PRIVATE) ?: return
        with(preferences.edit()) {
            putString(getString(R.string.saved_username), username)
            apply()
        }
    }

    private fun showUserName() {
        username = getPreferences(MODE_PRIVATE).getString(getString(R.string.saved_username), "")
            .toString()
        etUsername.setText(username)
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {
        val builder = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = builder.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        if (username.isNotEmpty()) {
            githubApi
                .getAllRepositoriesByUser(username)
                .enqueue(object : Callback<List<Repository>> {
                    override fun onResponse(
                        call: Call<List<Repository>>,
                        response: Response<List<Repository>>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                setupAdapter(it)
                            }
                        } else
                            Toast.makeText(
                                applicationContext,
                                R.string.response_error,
                                Toast.LENGTH_LONG
                            ).show()
                    }

                    override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                        Toast.makeText(
                            applicationContext,
                            R.string.response_error,
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                })
        }
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(repositories: List<Repository>) {
        val listAdapter = RepositoryAdapter(repositories, ::shareRepositoryLink, ::openBrowser)
        rvRepositories.apply {
            adapter = listAdapter
            visibility = RecyclerView.VISIBLE
        }
    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}