package path_manager;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

class exit extends WindowAdapter
{
	@Override
	public void windowClosing(WindowEvent event)
	{
		System.exit(0);
	}
}

class file_system
{
	public file_system()
	{
	}
	public static void del(String path)
	{
		File file=new File(path);
		file.delete();
	}
	public static boolean copy(String from, String to)
	{
		try
		{
			File from_file=new File(from);
			File to_file=new File(to);
			if(from_file.exists())
			{
				FileInputStream sfrom=new FileInputStream(from_file);
				FileOutputStream sto=new FileOutputStream(to_file);
				byte[] bytes=new byte[1024];
				while(sfrom.read(bytes)!=-1)
				{
					sto.write(bytes);
				}
				sfrom.close();
				sto.close();
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(Exception err)
		{
			return false;
		}
	}
}

class deleteFolder
{
	public deleteFolder(String path)
	{
		File file=new File(path);
		String[] files=file.list();
		int doing=0;
		while(doing<files.length)
		{
			File now=new File(path+"/"+files[doing]);
			if(now.isFile())
			{
				now.delete();
			}
			else
			{
				new deleteFolder(now.toString());
			}
			doing+=1;
		}
		file.delete();
	}
}

class frame extends Thread
{
	public JFrame root=new JFrame("Music player");
	public JSplitPane panel=new JSplitPane();
	public JPanel sidebar=new JPanel(new FlowLayout());
	public JProgressBar progress_loading=new JProgressBar();
	public JPanel main=new JPanel(new FlowLayout());
	public JButton load_button=new JButton();
	
	public ArrayList<String> list=new ArrayList<String>();
	public DefaultMutableTreeNode list_root=new DefaultMutableTreeNode("All");
	public JTree list_root_top=null;
	public ArrayList<DefaultMutableTreeNode> list_root_list=new ArrayList<DefaultMutableTreeNode>();
	public JScrollPane pic=null;
	public JLabel pic_show=new JLabel();
	public TreePath[] selection=null;
	public frame()
	{
	}
	public String[] getPath(String tree_path)
	{
		tree_path=tree_path.substring(1, tree_path.length()-1);
		return tree_path.split(", ");
	}
	public void run()
	{
		root.setSize(500, 500);
		root.setPreferredSize(new Dimension(500, 500));
		
		sidebar.setBorder(BorderFactory.createTitledBorder(""));
		sidebar.setSize(new Dimension(root.getWidth()/3*1, root.getHeight()));
		sidebar.add(new JLabel("Loading..."));
		progress_loading.setMinimum(0);
		progress_loading.setMaximum(100);
		progress_loading.setValue(0);
		progress_loading.setStringPainted(true);
		progress_loading.setPreferredSize(new Dimension(sidebar.getWidth()-5, 25));
		sidebar.add(progress_loading);
		panel.setLeftComponent(sidebar);
		
		main.setSize(new Dimension(root.getWidth()/3*2, root.getHeight()));
		JButton remove_button=new JButton();
		remove_button.setText("remove");
		remove_button.setPreferredSize(new Dimension(main.getWidth()-50, 32));
		remove_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SwingWorker<String, Object> delete=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						try
						{
							String path=getPath(selection[0].toString())[1];
							if(!path.equals("MyMusic"))
							{
								File file=new File("files/"+path);
								if(file.exists())
								{
									try
									{
										new deleteFolder(file.toString());
										ArrayList<String> albums=new ArrayList<String>();
										BufferedReader path_reader=new BufferedReader(new InputStreamReader(new FileInputStream(new File("player.list"))));
										String album;
										while((album=path_reader.readLine())!=null)
										{
											if(!album.equals(path) && !album.equals(null))
											{
												albums.add(album);
											}
										}
										path_reader.close();
										int doing=0;
										FileWriter writer=new FileWriter("player.list");
										while(doing<albums.size())
										{
											writer.write(albums.get(doing)+"\n");
											doing+=1;
										}
										writer.flush();
										writer.close();
									}
									catch(Exception err)
									{
										err.printStackTrace();
									}
									if(file.exists())
									{
										JOptionPane.showMessageDialog(null, "Error: con not delete the album, please close the player first");
									}
									else
									{
										JOptionPane.showMessageDialog(null, "OK");
										load_menu();
									}
								}
								else
								{
									JOptionPane.showMessageDialog(null, "OK");
									load_menu();
								}
							}
							else
							{
								JOptionPane.showMessageDialog(null, "Error: can not delete the album: MyMusic");
							}
						}
						catch(Exception err)
						{
							JOptionPane.showMessageDialog(null, "Error: please choose an album");
						}
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				delete.execute();
			}
				});
		main.add(remove_button);
		JButton remove_album_button=new JButton();
		remove_album_button.setText("remove from album");
		remove_album_button.setPreferredSize(new Dimension(main.getWidth()-50, 32));
		remove_album_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SwingWorker<String, Object> delete=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						try
						{
							String path=getPath(selection[0].toString())[1];
							String file="files/"+path+"/"+getPath(selection[0].toString())[2];
							if(new File(file).exists())
							{
								new File(file).delete();
								if(new File(file).exists())
								{
									JOptionPane.showMessageDialog(null, "Can not delete the file");
								}
								else
								{
									JOptionPane.showMessageDialog(null, "OK");
									load_menu();
								}
							}
							else
							{
								JOptionPane.showMessageDialog(null, "can not find the file: "+file);
							}
						}
						catch(Exception err)
						{
							JOptionPane.showMessageDialog(null, "Error: please choose an album");
						}
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				delete.execute();
			}
				});
		main.add(remove_album_button);
		JButton add_button=new JButton();
		add_button.setText("add files to existing album");
		add_button.setPreferredSize(new Dimension(main.getWidth()-50, 32));
		add_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SwingWorker<String, Object> add=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						try
						{
							String album=getPath(selection[0].toString())[1];
							if(new File("files/"+album).exists() && new File("files/"+album).isDirectory())
							{
								JFileChooser chooser=new JFileChooser();
								chooser.setMultiSelectionEnabled(true);
								chooser.setFileFilter(new FileNameExtensionFilter("audio(*.wav, *.mp3, *.m4a, *.pcm, *.mpeg, *.amr)", "wav", "mp3", "m4a", "pcm", "mpeg", "amr"));
								int res=chooser.showOpenDialog(main);
								if(res==JFileChooser.APPROVE_OPTION)
								{
									File[] files=chooser.getSelectedFiles();
									try
									{
										int doing=0;
										while(doing<files.length)
										{
											try
											{
												String file=files[doing].toString();
												file_system.copy(file, "files/"+album+"/"+file.toString().split(Matcher.quoteReplacement(File.separator))[file.toString().split(Matcher.quoteReplacement(File.separator)).length-1]);
											}
											catch(Exception err)
											{
											}
											doing+=1;
										}
									}
									catch(Exception err)
									{
									}
									JOptionPane.showMessageDialog(null, "OK");
									load_menu();
								}
								else
								{
									JOptionPane.showMessageDialog(null, "Error: you must choose a file first");
								}
							}
							else
							{
								JOptionPane.showMessageDialog(null, "Error");
							}
						}
						catch(Exception err)
						{
							JOptionPane.showMessageDialog(null, "Error");
						}
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				add.execute();
			}
				});
		main.add(add_button);
		JButton add_album_button=new JButton();
		add_album_button.setText("add an album");
		add_album_button.setPreferredSize(new Dimension(main.getWidth()-50, 32));
		add_album_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SwingWorker<String, Object> add=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						try
						{
							String path=JOptionPane.showInputDialog(root, "album name", "name");
							if(!(path==null))
							{
								boolean pass=false;
								int doing=0;
								while(doing<path.length())
								{
									if(!path.substring(doing, doing+1).equals(" "))
									{
										pass=true;
										break;
									}
									doing+=1;
								}
								if(pass==false)
								{
									JOptionPane.showMessageDialog(null, "Your name is not allowed");
								}
								else
								{
									path=path.replace("\\", " ");
									path=path.replace("/", " ");
									path=path.replace("*", " ");
									path=path.replace(":", " ");
									path=path.replace("|", " ");
									path=path.replace("?", " ");
									path=path.replace("<", " ");
									path=path.replace(">", " ");
									path=path.replace("\"", "\'");
									path=path.replace("\n", "");
									boolean exists=false;
									BufferedReader path_reader=new BufferedReader(new InputStreamReader(new FileInputStream(new File("player.list"))));
									String album;
									while((album=path_reader.readLine())!=null)
									{
										if(album.equals(path) || album.equals(path.replace(" ", "")) || path.equals("cover"))
										{
											exists=true;
										}
									}
									path_reader.close();
									if(exists==false)
									{
										FileWriter writer=new FileWriter("player.list", true);
										writer.write(path+"\n");
										writer.flush();
										writer.close();
										new File("files/"+path).mkdir();
										JOptionPane.showMessageDialog(null, "OK");
										load_menu();
									}
									else
									{
										JOptionPane.showMessageDialog(null, "The album is already exists");
									}
								}
							}
							else
							{
								JOptionPane.showMessageDialog(null, "Your name is null");
							}
						}
						catch(Exception err)
						{
							err.printStackTrace();
							JOptionPane.showMessageDialog(null, "Error: please choose an album");
						}
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				add.execute();
			}
				});
		main.add(add_album_button);
		load_button.setText("Refresh menu");
		load_button.setPreferredSize(new Dimension(main.getWidth()-50, 32));
		load_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				System.out.println("load menu");
				SwingWorker<String, Object> load=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						load_menu();
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				load.execute();
			}
				});
		main.add(load_button);
		
		panel.setRightComponent(main);
		panel.setOneTouchExpandable(true);
		panel.setDividerLocation(root.getWidth()/3*1);
		
		root.add(panel);
		root.pack();
		root.addWindowListener(new exit());
		root.setVisible(true);
		root.setResizable(false);
		root.setAlwaysOnTop(false);
		SwingWorker<String, Object> refresh=new SwingWorker<String, Object>(){
			@Override
			protected String doInBackground() throws Exception
			{
				load_menu_start();
				return null;
			}
			@Override
			protected void done()
			{
			}
		};
		refresh.execute();
	}
	public void load_menu()
	{
		if(sidebar!=null)
		{
			load_button.setEnabled(false);
			list_root.removeAllChildren();
			list.clear();
			list_root=new DefaultMutableTreeNode("All");
			list_root_top=null;
			list_root_list.clear();
			String name;
			try
			{
				BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream("player.list")));
				int doing=0;
				while((name=reader.readLine())!=null)
				{
					list.add(doing, name);
					doing+=1;
				}
				reader.close();
				doing=0;
				while(doing<list.size())
				{
					if(list.get(doing)==null)
					{
						break;
					}
					if(!(list.get(doing).indexOf(", ")==-1))
					{
						System.out.println("Error");
						System.exit(0);
					}
					File file=new File("files/"+list.get(doing).replace(", ", ""));
					String[] audio_files=file.list();
					if(!file.exists())
					{
						list_root.add(new DefaultMutableTreeNode(file.toString().substring(6)));
						doing+=1;
						continue;
					}
					int did=0;
					list_root_list.add(doing, new DefaultMutableTreeNode(file.toString().substring(6)));
					while((audio_files!=null)&&(did<audio_files.length))
					{
						list_root_list.get(doing).add(new DefaultMutableTreeNode(audio_files[did]));
						did+=1;
					}
					list_root.add(list_root_list.get(doing));
					doing+=1;
				}
			}
			catch(Exception err)
			{
				err.printStackTrace();
			}
			progress_loading.setValue(100);
			sidebar.repaint();
			try
			{
				Thread.sleep(1000);
			}
			catch(Exception err)
			{
			}
			sidebar.removeAll();
			list_root_top=new JTree(list_root);
			list_root_top.addTreeSelectionListener(new TreeSelectionListener()
					{
				@Override
				public void valueChanged(TreeSelectionEvent event)
				{
					selection=list_root_top.getSelectionPaths();
				}
					});
			JScrollPane tree=new JScrollPane(list_root_top);
			tree.setPreferredSize(new Dimension(sidebar.getWidth()-5, sidebar.getHeight()-50));
			list_root_top.setSelectionPath(list_root_top.getPathForRow(1));
			tree.repaint();
			sidebar.add(tree);
			tree.repaint();
			sidebar.repaint();
			panel.setLeftComponent(null);
			panel.setLeftComponent(sidebar);
			panel.setDividerLocation(root.getWidth()/3*1);
			load_button.setEnabled(true);
		}
	}
	public void load_menu_start()
	{
		if(sidebar!=null)
		{
			load_button.setEnabled(false);
			list_root.removeAllChildren();
			list.clear();
			list_root=new DefaultMutableTreeNode("All");
			list_root_top=null;
			list_root_list.clear();
			String name;
			try
			{
				BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream("player.list")));
				int doing=0;
				while((name=reader.readLine())!=null)
				{
					list.add(doing, name);
					doing+=1;
				}
				reader.close();
				doing=0;
				while(doing<list.size())
				{
					progress_loading.setValue(Math.round(100/list.size()*doing));
					sidebar.repaint();
					Thread.sleep(1000);
					if(list.get(doing)==null)
					{
						break;
					}
					if(!(list.get(doing).indexOf(", ")==-1))
					{
						System.out.println("Error");
						System.exit(0);
					}
					File file=new File("files/"+list.get(doing).replace(", ", ""));
					String[] audio_files=file.list();
					if(!file.exists())
					{
						list_root.add(new DefaultMutableTreeNode(file.toString().substring(6)));
						doing+=1;
						continue;
					}
					int did=0;
					list_root_list.add(doing, new DefaultMutableTreeNode(file.toString().substring(6)));
					while((audio_files!=null)&&(did<audio_files.length))
					{
						list_root_list.get(doing).add(new DefaultMutableTreeNode(audio_files[did]));
						did+=1;
					}
					list_root.add(list_root_list.get(doing));
					doing+=1;
				}
			}
			catch(Exception err)
			{
				err.printStackTrace();
			}
			progress_loading.setValue(100);
			sidebar.repaint();
			try
			{
				Thread.sleep(1000);
			}
			catch(Exception err)
			{
			}
			sidebar.removeAll();
			list_root_top=new JTree(list_root);
			list_root_top.addTreeSelectionListener(new TreeSelectionListener()
					{
				@Override
				public void valueChanged(TreeSelectionEvent event)
				{
					selection=list_root_top.getSelectionPaths();
				}
					});
			JScrollPane tree=new JScrollPane(list_root_top);
			tree.setPreferredSize(new Dimension(sidebar.getWidth()-5, sidebar.getHeight()-50));
			list_root_top.setSelectionPath(list_root_top.getPathForRow(1));
			tree.repaint();
			sidebar.add(tree);
			tree.repaint();
			sidebar.repaint();
			panel.setLeftComponent(null);
			panel.setLeftComponent(sidebar);
			panel.setDividerLocation(root.getWidth()/3*1);
			load_button.setEnabled(true);
		}
	}
	public static void createFolder(String path, boolean keep_desk, String add)
	{
		String name=add;
		path.replace("\\", "/");
		String[] names=path.split("/");
		int doing=0;
		if(keep_desk==false)
		{
			doing=1;
		}
		while(doing<names.length)
		{
			name+=names[doing]+"/";
			if(new File(name).exists())
			{
			}
			else
			{
				new File(name).mkdir();
			}
			doing+=1;
		}
		System.out.println(name);
	}
}

public class path_manager
{
	public static void main(String [] args)
	{
		new frame().start();
	}
}