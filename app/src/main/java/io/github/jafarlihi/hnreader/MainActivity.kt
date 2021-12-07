package io.github.jafarlihi.hnreader

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import io.github.jafarlihi.hnreader.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private var currentPage: Int = 1
    private lateinit var currentPageDocument: Document
    private lateinit var linearLayout: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var mainContext: Context

    @ObsoleteCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainContext = this

        linearLayout = findViewById(R.id.main_layout)
        scrollView = findViewById(R.id.main_scroll_view)

        linearLayout.gravity = Gravity.CENTER

        refresh()
    }

    @ObsoleteCoroutinesApi
    fun refresh() {
        linearLayout.removeAllViews()
        insertLogo()

        runBlocking {
            launch(newSingleThreadContext("fetchThread")) {
                currentPageDocument = fetchPage(currentPage);

                for (item in fetchItems(currentPageDocument)) {
                    val item: Item = parseItem(item)
                    val itemView: TextView = TextView(mainContext)
                    itemView.setGravity(Gravity.CENTER)
                    itemView.setText(resources.getString(R.string.item_view, item.title, item.shortUrl))
                    itemView.isClickable = true
                    itemView.setOnClickListener {

                    }
                    linearLayout.addView(itemView)

                    val line: View = View(mainContext);
                    line.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 5));
                    linearLayout.addView(line)
                }

                insertButtons()
                // TODO: Figure out why calling fullScroll once fails sometimes
                for (i in 1..10) scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        }
    }

    @ObsoleteCoroutinesApi
    fun insertButtons() {
        val buttonLayout: LinearLayout = LinearLayout(this)
        buttonLayout.orientation = LinearLayout.HORIZONTAL
        buttonLayout.gravity = Gravity.CENTER

        val backButton: ImageButton = ImageButton(this)
        backButton.foregroundGravity = Gravity.CENTER
        backButton.setImageDrawable(resources.getDrawable(R.mipmap.ic_back_icon))
        backButton.setOnClickListener {
            currentPage--
            refresh()
        }
        if (currentPage == 1) backButton.isEnabled = false
        buttonLayout.addView(backButton)

        val forwardButton: ImageButton = ImageButton(this)
        forwardButton.foregroundGravity = Gravity.CENTER
        forwardButton.setImageDrawable(resources.getDrawable(R.mipmap.ic_forward_icon))
        forwardButton.setOnClickListener {
            currentPage++
            refresh()
        }
        buttonLayout.addView(forwardButton)

        linearLayout.addView(buttonLayout)

        val pageNumberView: TextView = TextView(this)
        pageNumberView.text = resources.getString(R.string.page_number, currentPage)
        pageNumberView.gravity = Gravity.CENTER

        linearLayout.addView(pageNumberView)
    }

    fun insertLogo() {
        val headerView: ImageView = ImageView(this)
        headerView.setImageDrawable(resources.getDrawable(R.drawable.ic_hn_logo))
        linearLayout.addView(headerView)
        headerView.layoutParams.height = 100
        headerView.layoutParams.width = 100
        headerView.foregroundGravity = Gravity.CENTER
    }

    data class Item(val title: String, val url: String, val shortUrl: String, val points: String)

    fun fetchPage(page: Int): Document {
        return Jsoup.connect("https://news.ycombinator.com/news?p=" + page).get()
    }

    fun fetchItems(page: Document): Elements {
        return page.select(".athing")
    }

    fun parseItem(element: Element): Item {
        return Item(
            element.select(".title .titlelink").text(),
            element.select(".title a").attr("href"),
            element.select(".title .sitestr").text(),
            "unimplemented"
        )
    }
}
